/*
 * Copyright 2002-2023 the original author or authors.
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.DataLoaderOptions;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

final class DefaultFunctionBatchLoaderSpec<K, V> implements FunctionBatchLoaderSpec<K, V> {

    private final String name;

    @Nullable
    private final DataLoaderOptions dataLoaderOptions;

    private final BiFunction<?, BatchLoaderEnvironment, ?> loader;

    private final boolean isMapped;

    DefaultFunctionBatchLoaderSpec(String name, BiFunction<?, BatchLoaderEnvironment, ?> loader, boolean isMapped) {
        this(name, null, loader, isMapped);
    }

    DefaultFunctionBatchLoaderSpec(String name, @Nullable DataLoaderOptions dataLoaderOptions, BiFunction<?, BatchLoaderEnvironment, ?> loader, boolean isMapped) {
        Assert.notNull(name, "'name' is required");
        Assert.notNull(loader, "'loader' is required");

        this.name = name;
        this.dataLoaderOptions = dataLoaderOptions;
        this.loader = loader;
        this.isMapped = isMapped;
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

    @Nullable
    @Override
    public BiFunction<List<K>, BatchLoaderEnvironment, Flux<V>> getNonMappedLoader() {
        try {
            return (BiFunction<List<K>, BatchLoaderEnvironment, Flux<V>>) this.loader;
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    @Nullable
    @Override
    public BiFunction<Set<K>, BatchLoaderEnvironment, Mono<Map<K, V>>> getMappedLoader() {
        try {
            return (BiFunction<Set<K>, BatchLoaderEnvironment, Mono<Map<K, V>>>) this.loader;
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    @Override
    public boolean isMapped() {
        return this.isMapped;
    }
}
