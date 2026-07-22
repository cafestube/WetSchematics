package eu.cafestube.schematics.paper;

import java.util.HashMap;
import java.util.Map;

class VersionProvider {

    private static final Map<String, String> VERSION_TO_REVISION = new HashMap<>() {
        {
            this.put("1.21.1", "1211");
            this.put("1.21.2", "1211");
            this.put("1.21.3", "1213");
            this.put("1.21.4", "1214");
            this.put("1.21.5", "1214");
            this.put("1.21.6", "1216");
            this.put("1.21.7", "1216");
            this.put("1.21.8", "1216");
            this.put("1.21.9", "1216");
            this.put("1.21.10", "1216");
            this.put("1.21.11", "1216");

            this.put("26.2", "262");
        }
    };
    private static final String FALLBACK_REVISION = "262";

    public static String getRevisionName(String version) {
        return VERSION_TO_REVISION.getOrDefault(version, FALLBACK_REVISION);
    }

}
