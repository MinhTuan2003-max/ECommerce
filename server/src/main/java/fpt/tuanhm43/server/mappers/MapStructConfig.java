package fpt.tuanhm43.server.mappers;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * MapStruct Global Configuration
 */
@MapperConfig(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface MapStructConfig {
}