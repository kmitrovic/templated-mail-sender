package org.cobbzilla.util.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * (c) Copyright 2013 Jonathan Cobb.
 * This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html
 */
public class LocaleAwareMustacheFactory extends DefaultMustacheFactory {

    private static final Logger LOG = LoggerFactory.getLogger(LocaleAwareMustacheFactory.class);

    public static final String DEFAULT_LOCALE = "en_US";
    public static final String TEMPLATE_SUFFIX = ".mustache";

    private final File superFileRoot; // superclass should have this protected, not private
    private final List<String> suffixChecks;

    protected static final LoadingCache<LAMFCacheKey, LocaleAwareMustacheFactory> factoryLoadingCache
            = CacheBuilder.newBuilder().build(new CacheLoader<LAMFCacheKey, LocaleAwareMustacheFactory>() {
        @Override
        public LocaleAwareMustacheFactory load(LAMFCacheKey key) throws Exception {
            return new LocaleAwareMustacheFactory(key.fileRoot, key.locale);
        }
    });

    public static LocaleAwareMustacheFactory getFactory (File fileRoot, Locale locale) throws ExecutionException {
        return getFactory(fileRoot, (locale == null) ? DEFAULT_LOCALE : locale.toString());
    }

    private static final AtomicLong lastRefresh = new AtomicLong(System.currentTimeMillis());
    private static final long REFRESH_INTERVAL = 1000 * 60 * 5; // 5 minutes

    public synchronized static LocaleAwareMustacheFactory getFactory(File fileRoot, String locale) throws ExecutionException {
        if (locale == null) locale = DEFAULT_LOCALE;
        if (System.currentTimeMillis() > lastRefresh.longValue() + REFRESH_INTERVAL) {
            factoryLoadingCache.invalidateAll();
        }
        return factoryLoadingCache.get(new LAMFCacheKey(fileRoot, locale));
    }

    public LocaleAwareMustacheFactory (File fileRoot, Locale locale) {
        this(fileRoot, locale.toString());
    }

    public LocaleAwareMustacheFactory (File fileRoot, String locale) {
        super(fileRoot);
        this.superFileRoot = fileRoot;
        this.suffixChecks = new ArrayList<>(4);
        StringBuilder suffix = new StringBuilder();
        for (String localePart : locale.split("_")) {
            if (suffix.length() > 0) suffix.append('_');
            suffix.append(localePart);
            suffixChecks.add(0, suffix.toString());
        }
        suffixChecks.add(""); // default template
    }

    private class LAMFCacheLoader extends CacheLoader<String, Mustache> {
        @Override
        public Mustache load(String key) throws Exception {
//            return mc.compile(superFileRoot.getAbsolutePath()+"/"+key);
            return mc.compile(key);
        }
    }

    @Override
    protected LoadingCache<String, Mustache> createMustacheCache() {
        return CacheBuilder.newBuilder().build(new LAMFCacheLoader());
    }

    @Override
    public Reader getReader(String resourceName) {
        for (String suffix : suffixChecks) {
            // don't add a trailing _ if there is not suffix, since the default resource is
            // simply "path/to/resourceName" not "path/to/resourceName_"
            final String name = (suffix.length() == 0) ? resourceName + TEMPLATE_SUFFIX : resourceName + "_" + suffix + TEMPLATE_SUFFIX;
            try {
                return super.getReader(name);
            } catch (MustacheException e) {
                LOG.debug("getReader: didn't find resource at "+name);
            }
        }
        throw new MustacheResourceNotFoundException("getReader: no resource (not even a default resource) found at all: "+resourceName);
    }

    private static class LAMFCacheKey {

        public File fileRoot;
        public String locale;

        public LAMFCacheKey(File fileRoot, String locale) {
            this.fileRoot = fileRoot;
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LAMFCacheKey)) return false;

            LAMFCacheKey that = (LAMFCacheKey) o;

            if (!fileRoot.equals(that.fileRoot)) return false;
            if (!locale.equals(that.locale)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = fileRoot.hashCode();
            result = 31 * result + locale.hashCode();
            return result;
        }
    }
}
