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

import org.dataloader.BatchLoader;
import org.dataloader.BatchLoaderWithContext;
import org.dataloader.DataLoaderOptions;
import org.dataloader.MappedBatchLoader;
import org.dataloader.MappedBatchLoaderWithContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * A specification of options available for registering a batch loader
 * that is to be used to eventually create a {@link org.dataloader.DataLoader}.
 */
public interface BatchLoaderSpec {

    /**
     * @return name to be used for the {@link org.dataloader.DataLoader}
     * that will be created for this spec.
     */
    String getName();

    /**
     * @return data loader options to be used for the
     * {@link org.dataloader.DataLoader} that will be created for this spec.
     */
    @Nullable
    DataLoaderOptions getDataLoaderOptions();

    /**
     * <strong>NOTE:</strong> Though the return type is {@link Object}, the
     * returned value must be one of the following:
     * <ul>
     *   <li>{@link org.dataloader.BatchLoader}</li>
     *   <li>{@link org.dataloader.BatchLoaderWithContext}</li>
     *   <li>{@link org.dataloader.MappedBatchLoader}</li>
     *   <li>{@link org.dataloader.MappedBatchLoaderWithContext}</li>
     *   <li>{@link BatchLoaderWithContextAndOptions}</li>
     *   <li>{@link BatchLoaderWithOptions}</li>
     *   <li>{@link MappedBatchLoaderWithContextAndOptions}</li>
     *   <li>{@link MappedBatchLoaderWithOptions}</li>
     * </ul>
     *
     * @return the batch loader to be used for the
     * {@link org.dataloader.DataLoader} that will be created for this spec.
     */
    Object getLoader();


    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(BatchLoader<?, ?> loader) {
        return new DefaultBatchLoaderSpec(loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(String name, BatchLoader<?, ?> loader) {
        return new DefaultBatchLoaderSpec(name, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(@Nullable DataLoaderOptions options, BatchLoader<?, ?> loader) {
        return new DefaultBatchLoaderSpec(options, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(String name, @Nullable DataLoaderOptions options, BatchLoader<?, ?> loader) {
        return new DefaultBatchLoaderSpec(name, options, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(BatchLoaderWithContext<?, ?> loader) {
        return new DefaultBatchLoaderSpec(loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(String name, BatchLoaderWithContext<?, ?> loader) {
        return new DefaultBatchLoaderSpec(name, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(@Nullable DataLoaderOptions options, BatchLoaderWithContext<?, ?> loader) {
        return new DefaultBatchLoaderSpec(options, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(String name, @Nullable DataLoaderOptions options, BatchLoaderWithContext<?, ?> loader) {
        return new DefaultBatchLoaderSpec(name, options, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(MappedBatchLoader<?, ?> loader) {
        return new DefaultBatchLoaderSpec(loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(String name, MappedBatchLoader<?, ?> loader) {
        return new DefaultBatchLoaderSpec(name, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(@Nullable DataLoaderOptions options, MappedBatchLoader<?, ?> loader) {
        return new DefaultBatchLoaderSpec(options, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(String name, @Nullable DataLoaderOptions options, MappedBatchLoader<?, ?> loader) {
        return new DefaultBatchLoaderSpec(name, options, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(MappedBatchLoaderWithContext<?, ?> loader) {
        return new DefaultBatchLoaderSpec(loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(String name, MappedBatchLoaderWithContext<?, ?> loader) {
        return new DefaultBatchLoaderSpec(name, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(@Nullable DataLoaderOptions options, MappedBatchLoaderWithContext<?, ?> loader) {
        return new DefaultBatchLoaderSpec(options, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(String name, @Nullable DataLoaderOptions options, MappedBatchLoaderWithContext<?, ?> loader) {
        return new DefaultBatchLoaderSpec(name, options, loader);
    }

    /**
     * TODO
     *
     * @param loader
     */
    static BatchLoaderSpec create(SpringBatchLoaderWithOptions loader) {
        return new DefaultBatchLoaderSpec(loader.getName(), loader.getDataLoaderOptions(), loader);
    }
}
