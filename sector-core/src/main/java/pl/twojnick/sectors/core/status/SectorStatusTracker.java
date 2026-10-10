package pl.twojnick.sectors.core.status;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SectorStatusTracker {
    private final Map<String, Boolean> sectorStatus = new ConcurrentHashMap<>();

    public void updateStatus(String sectorId, boolean isOnline) {
        sectorStatus.put(sectorId, isOnline);
    }

    public boolean isSectorActive(String sectorId) {
        return sectorStatus.getOrDefault(sectorId, false);
    }
}
