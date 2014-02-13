package org.wso2.carbon.mediation.statistics;

public class StatisticsConstants {

    //Possible statistics categories
    public static final int ENDPOINT_STATISTICS = 0;
    public static final int PROXY_SERVICE_STATISTICS = 1;
    public static final int SEQUENCE_STATISTICS = 2;

    //Posible directions
    public static final int IN = 0;
    public static final int OUT = 1;

    public static final String TOTAL_COUNT = "totalCount";
    public static final String FAULT_COUNT = "faultCount";
    public static final String MAXIMUM_TIME = "maxTime";
    public static final String MINIMUM_TIME = "minTime";
    public static final String AVERAGE_TIME = "avgTime";

    // Carbon XML properties
    public static final String STAT_CONFIG_ELEMENT = "MediationStat";
    public static final String STAT_REPORTING_INTERVAL = STAT_CONFIG_ELEMENT + ".ReportingInterval";
    public static final String STAT_PERSISTENCE = STAT_CONFIG_ELEMENT + ".Persistence";
    public static final String STAT_PERSISTENCE_ROOT = STAT_CONFIG_ELEMENT + ".RegistryLocation";
    public static final String STAT_OBSERVERS = STAT_CONFIG_ELEMENT + ".Observers";
    public static final String STAT_TRACING = STAT_CONFIG_ELEMENT + ".MessageTracing";

    public static final String STAT_PROPERTY = "MediationStatisticsStore";

}
