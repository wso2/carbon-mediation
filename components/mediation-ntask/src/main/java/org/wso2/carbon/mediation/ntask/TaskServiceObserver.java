package org.wso2.carbon.mediation.ntask;

import java.util.Map;

public interface TaskServiceObserver {
    public boolean update(Map<String, Object> parameters);
}
