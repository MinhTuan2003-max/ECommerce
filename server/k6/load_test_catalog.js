import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 50 }, // Tăng dần lên 50 users trong 30s
        { duration: '1m', target: 200 }, // Giữ 200 users trong 1 phút (Giả lập cao điểm)
        { duration: '30s', target: 0 },  // Giảm dần về 0
    ],
};

const BASE_URL = 'http://localhost:8080/api/v1';

export default function () {
    // Giả lập hành vi người dùng lướt web

    let resSearch = http.post(`${BASE_URL}/products/search`, JSON.stringify({
        keyword: "áo thun",
        minPrice: 0,
        maxPrice: 500000
    }), { headers: { 'Content-Type': 'application/json' } });

    check(resSearch, { 'Search status 200': (r) => r.status === 200 });

    sleep(2);
}