package io.prometheus.cloudwatch;

public interface Resolver {

    String fromLabel();
    String newLabel();
    String resourceFromCloudwatchDimension(String dimension);
    String resolve(String resource);
}
