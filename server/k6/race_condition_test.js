import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// --- CẤU HÌNH ---
const BASE_URL = 'http://localhost:8080/api/v1';

// ⚠️ QUAN TRỌNG: Thay ID này bằng ID thật của sản phẩm đang có trong DB của bạn
// ID "3fa85f..." thường là ID ảo của Swagger, chạy sẽ bị lỗi 404 Product Not Found
const VARIANT_ID = '27d5a989-5c08-459d-af7d-a90b1ba28954';

const VUS = 50;

export const options = {
    vus: VUS,
    iterations: VUS, // Mỗi user chạy 1 luồng trọn vẹn
};

export default function () {
    // 1. Tạo Session ID khởi tạo
    let currentSessionId = uuidv4();

    // Header chung
    let params = {
        headers: {
            'Content-Type': 'application/json',
            'X-Session-Id': currentSessionId, // Gửi ID tự tạo lên trước
        },
    };

    // --- BƯỚC 1: THÊM VÀO GIỎ HÀNG (ADD TO CART) ---
    const addToCartPayload = JSON.stringify({
        variantId: VARIANT_ID,
        quantity: 1
    });

    const resCart = http.post(`${BASE_URL}/cart/add`, addToCartPayload, params);

    // Kiểm tra xem thêm giỏ hàng có thành công không
    const isCartSuccess = check(resCart, {
        'Step 1: Add to cart success (200/201)': (r) => r.status === 200 || r.status === 201,
    });

    // LOGIC THÔNG MINH: Nếu server trả về SessionId mới, hãy cập nhật nó!
    // Dựa trên response mẫu bạn gửi: data.sessionId
    if (isCartSuccess) {
        try {
            const body = resCart.json();
            if (body.data && body.data.sessionId) {
                currentSessionId = body.data.sessionId;
                // Cập nhật lại header với ID chính chủ từ server cấp
                params.headers['X-Session-Id'] = currentSessionId;
            }
        } catch (e) {
            console.log('Không parse được JSON response từ cart, giữ nguyên Session ID cũ');
        }
    } else {
        // Nếu thêm giỏ hàng thất bại thì không cần chạy bước mua làm gì
        console.log(`User ${__VU} Add Cart Failed: ${resCart.status} ${resCart.body}`);
        return;
    }

    // Nghỉ 1 xíu để tăng khả năng xảy ra xung đột (Race Condition)
    sleep(0.1);

    // --- BƯỚC 2: TẠO ĐƠN HÀNG (ORDER FROM CART) ---
    const orderPayload = JSON.stringify({
        customerName: "Stress Tester",
        customerEmail: `tester${__VU}@example.com`,
        customerPhone: "0735130901",
        shippingAddress: "123 Street",
        paymentMethod: "COD",
        items: [
            {
                variantId: VARIANT_ID,
                quantity: 1
            }
        ]
    });

    const resOrder = http.post(`${BASE_URL}/orders/from-cart`, orderPayload, params);

    // --- KIỂM TRA KẾT QUẢ CUỐI CÙNG ---
    check(resOrder, {
        'Step 2: Order Success (201)': (r) => r.status === 201,
        'Step 2: Sold Out/Conflict (400/409)': (r) => r.status === 400 || r.status === 409,
        'Step 2: Server Error (500)': (r) => r.status === 500, // Lỗi Optimistic Lock nằm ở đây
    });
}