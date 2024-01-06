/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.graphql.execution;

import java.beans.Introspector;

import org.dataloader.DataLoaderOptions;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * TODO
 */
class DefaultBatchLoaderSpec implements BatchLoaderSpec {

    private final String name;

    @Nullable
    private final DataLoaderOptions dataLoaderOptions;

    private final Object loader;

    DefaultBatchLoaderSpec(Object loader) {
        this(null, null, loader);
    }

    DefaultBatchLoaderSpec(@Nullable String name, Object loader) {
        this(name, null, loader);
    }

    DefaultBatchLoaderSpec(@Nullable DataLoaderOptions dataLoaderOptions, Object loader) {
        this(null, dataLoaderOptions, loader);
    }

    DefaultBatchLoaderSpec(@Nullable String name, @Nullable DataLoaderOptions dataLoaderOptions, Object loader) {
        Assert.notNull(loader, "'loader' is required");

        this.name = (name == null ? Introspector.decapitalize(loader.getClass().getSimpleName()).replaceFirst("BatchLoader$", "DataLoader") : name);
        this.dataLoaderOptions = dataLoaderOptions;
        this.loader = loader;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Nullable
    @Override
    public DataLoaderOptions getDataLoaderOptions() {
        return this.dataLoaderOptions;
    }

    @Override
    public Object getLoader() {
        return this.loader;
    }
}
