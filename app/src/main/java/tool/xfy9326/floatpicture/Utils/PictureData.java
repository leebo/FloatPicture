package tool.xfy9326.floatpicture.Utils;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;

import tool.xfy9326.floatpicture.Methods.CodeMethods;
import tool.xfy9326.floatpicture.Methods.IOMethods;

public class PictureData {

    private static final String DataFileName = "PictureData.list";
    private static final String ListFileName = "PictureList.list";
    private String id;
    private JSONObject detailObject;
    private JSONObject listObject;
    private JSONObject dataObject;
    
    // Simple cache to avoid repeated file reads
    private static JSONObject cachedListObject;
    private static JSONObject cachedDataObject;
    private static long lastCacheTime = 0;
    private static final long CACHE_TIMEOUT = 5000; // 5 seconds

    public PictureData() {
    }

    public void setDataControl(String id) {
        this.id = id;
        this.listObject = getJSONFile(ListFileName);
        this.dataObject = getJSONFile(DataFileName);
        this.detailObject = getDetailObject(this.id);
    }

    @SuppressWarnings("SameParameterValue")
    public void put(String name, boolean value) {
        try {
            detailObject.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void put(String name, String value) {
        try {
            detailObject.put(name, CodeMethods.unicodeEncode(value));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void put(String name, int value) {
        try {
            detailObject.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("SameParameterValue")
    public void put(String name, float value) {
        try {
            detailObject.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("SameParameterValue")
    public boolean getBoolean(String name, boolean defaultValue) {
        if (detailObject.has(name)) {
            try {
                return detailObject.getBoolean(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unused")
    public String getString(String name, String defaultValue) {
        if (detailObject.has(name)) {
            try {
                return CodeMethods.unicodeDecode(detailObject.getString(name));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

    public int getInt(String name, int defaultValue) {
        if (detailObject.has(name)) {
            try {
                return detailObject.getInt(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("SameParameterValue")
    public float getFloat(String name, float defaultValue) {
        if (detailObject.has(name)) {
            try {
                return Float.parseFloat(detailObject.get(name) + "f");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

    public void commit(String pictureName) {
        try {
            if (pictureName != null) {
                listObject.put(id, pictureName);
            }
            dataObject.put(id, detailObject);
            setJSONFile(ListFileName, listObject);
            setJSONFile(DataFileName, dataObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void remove() {
        if (listObject.has(id)) {
            listObject.remove(id);
            dataObject.remove(id);
            setJSONFile(ListFileName, listObject);
            setJSONFile(DataFileName, dataObject);
        }
    }

    private JSONObject getDetailObject(String id) {
        if (dataObject.has(id)) {
            try {
                return dataObject.getJSONObject(id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }

    public LinkedHashMap<String, String> getListArray() {
        JSONObject listObject = getJSONFile(ListFileName);
        try {
            Iterator<String> iterator = listObject.keys();
            LinkedHashMap<String, String> arr = new LinkedHashMap<>();
            
            // First collect all entries into a temporary map
            java.util.Map<String, String> tempMap = new java.util.HashMap<>();
            String key;
            while (iterator.hasNext()) {
                key = iterator.next();
                tempMap.put(key, listObject.getString(key));
            }
            
            // Sort by key (which contains timestamp) in descending order (newest first)
            java.util.List<java.util.Map.Entry<String, String>> sortedEntries = 
                new java.util.ArrayList<>(tempMap.entrySet());
            sortedEntries.sort((entry1, entry2) -> {
                try {
                    // Extract timestamp from ID (before the first dash)
                    String id1 = entry1.getKey();
                    String id2 = entry2.getKey();
                    long timestamp1 = Long.parseLong(id1.split("-")[0]);
                    long timestamp2 = Long.parseLong(id2.split("-")[0]);
                    // Sort descending (newest first)
                    return Long.compare(timestamp2, timestamp1);
                } catch (Exception e) {
                    // If parsing fails, use string comparison
                    return entry2.getKey().compareTo(entry1.getKey());
                }
            });
            
            // Add sorted entries to LinkedHashMap to preserve order
            for (java.util.Map.Entry<String, String> entry : sortedEntries) {
                arr.put(entry.getKey(), entry.getValue());
            }
            
            return arr;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject getJSONFile(String FileName) {
        long currentTime = System.currentTimeMillis();
        
        // Use cache if available and not expired
        if (currentTime - lastCacheTime < CACHE_TIMEOUT) {
            if (FileName.equals(ListFileName) && cachedListObject != null) {
                return cachedListObject;
            }
            if (FileName.equals(DataFileName) && cachedDataObject != null) {
                return cachedDataObject;
            }
        }
        
        // Read from file
        String content = IOMethods.readFile(Config.DEFAULT_DATA_DIR + FileName);
        JSONObject result = new JSONObject();
        if (content != null) {
            try {
                if (!content.isEmpty()) {
                    result = new JSONObject(content);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        // Update cache
        if (FileName.equals(ListFileName)) {
            cachedListObject = result;
        } else if (FileName.equals(DataFileName)) {
            cachedDataObject = result;
        }
        lastCacheTime = currentTime;
        
        return result;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean setJSONFile(String FileName, JSONObject jsonObject) {
        // Clear cache when writing to force refresh on next read
        if (FileName.equals(ListFileName)) {
            cachedListObject = null;
        } else if (FileName.equals(DataFileName)) {
            cachedDataObject = null;
        }
        return IOMethods.writeFile(jsonObject.toString(), Config.DEFAULT_DATA_DIR + FileName);
    }

}
