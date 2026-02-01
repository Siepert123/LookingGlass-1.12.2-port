package com.xcompwiz.lookingglass.apiimpl;

import com.xcompwiz.lookingglass.api.APIInstanceProvider;
import com.xcompwiz.lookingglass.api.APIUndefined;
import com.xcompwiz.lookingglass.api.APIVersionRemoved;
import com.xcompwiz.lookingglass.api.APIVersionUndefined;

import java.util.*;

public class APIProviderImpl implements APIInstanceProvider {
    private final String modname;

    public APIProviderImpl(String modname) {
        this.modname = modname;
    }

    public String getOwnerMod() {
        return this.modname;
    }

    private final HashMap<String, Object> instances = new HashMap<>();

    @Override
    public Object getAPIInstance(String api) throws APIUndefined, APIVersionUndefined, APIVersionRemoved {
        Object ret = this.instances.get(api);
        if (ret != null) return ret;
        String[] splitName = api.split("-");
        if (splitName.length != 2) throw new APIUndefined(api);
        String apiName = splitName[0];
        int version = Integer.parseInt(splitName[1]);
        ret = constructAPIWrapper(this.modname, apiName, version);
        instances.put(api, ret);
        return ret;
    }

    private static Map<String, Map<Integer, WrapperBuilder>> apiCtors;
    private static Map<String, Set<Integer>> apiVersions;
    private static Map<String, Set<Integer>> apiVersionsImmutableSets;
    private static Map<String, Set<Integer>> apiVersionsImmutable;

    public static void init() {
        if (apiCtors != null) return;
        apiCtors = new HashMap<>();
        apiVersions = new HashMap<>();
        apiVersionsImmutableSets = new HashMap<>();
        apiVersionsImmutable = Collections.unmodifiableMap(apiVersions);

        registerAPI("view", 1, new WrapperBuilder(LookingGlassAPIWrapper.class));
        registerAPI("view", 2, new WrapperBuilder(LookingGlassAPI2Wrapper.class));
    }

    private static void registerAPI(String apiName, int version, WrapperBuilder builder) {
        getVersions(apiName).add(version);
        getCtors(apiName).put(version, builder);
    }

    private static Map<Integer, WrapperBuilder> getCtors(String apiName) {
        return apiCtors.computeIfAbsent(apiName, k -> new HashMap<>());
    }

    private static Set<Integer> getVersions(String apiName) {
        Set<Integer> versions = apiVersions.get(apiName);
        if (versions == null) {
            versions = new HashSet<>();
            apiVersions.put(apiName, versions);
            apiVersionsImmutableSets.put(apiName, Collections.unmodifiableSet(versions));
        }
        return versions;
    }

    private static Object constructAPIWrapper(String owner, String apiName, int version) throws APIUndefined, APIVersionUndefined, APIVersionRemoved {
        if (apiCtors == null) throw new RuntimeException("Something is broken. The LookingGlass API hasn't constructed properly.");
        Map<Integer, WrapperBuilder> ctors = apiCtors.get(apiName);
        if (ctors == null) throw new APIUndefined(apiName);
        if (!ctors.containsKey(version)) throw new APIVersionUndefined(apiName + "-" + version);
        WrapperBuilder ctor = ctors.get(version);
        if (ctor == null) throw new APIVersionRemoved(apiName + "-" + version);
        try {
            return ctor.newInstance(owner);
        } catch (Exception e) {
            throw new RuntimeException("Caught an exception while building an API wrapper. Go kick XCompWiz. Or Siepert, for that matter.", e);
        }
    }

    @Override
    public Map<String, Set<Integer>> getAvailableAPIs() {
        return apiVersionsImmutable;
    }
}
