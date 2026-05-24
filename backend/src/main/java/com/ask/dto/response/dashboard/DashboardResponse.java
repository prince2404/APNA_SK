package com.ask.dto.response.dashboard;

import lombok.*;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardResponse {
    private List<MetricCard> metrics;
    private List<ChartDataPoint> trendData;
    private List<ChartDataPoint> breakdownData;
    private List<TopProductPoint> topProducts;
    private List<RecentActivityPoint> recentActivity;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MetricCard {
        private String label;
        private String value;
        private String change;
        private String type;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ChartDataPoint {
        private String name;
        private Double value;
        private Double secondaryValue;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopProductPoint {
        private String productName;
        private Integer quantity;
        private Double totalRevenue;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RecentActivityPoint {
        private String description;
        private String timestamp;
        private String type;
    }
}
