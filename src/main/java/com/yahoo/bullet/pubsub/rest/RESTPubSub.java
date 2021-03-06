/*
 *  Copyright 2018, Yahoo Inc.
 *  Licensed under the terms of the Apache License, Version 2.0.
 *  See the LICENSE file associated with the project for terms.
 */
package com.yahoo.bullet.pubsub.rest;

import com.yahoo.bullet.common.BulletConfig;
import com.yahoo.bullet.pubsub.PubSub;
import com.yahoo.bullet.pubsub.PubSubException;
import com.yahoo.bullet.pubsub.Publisher;
import com.yahoo.bullet.pubsub.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.HttpClients;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class RESTPubSub extends PubSub {
    public static final int OK_200 = 200;
    public static final int NO_CONTENT_204 = 204;
    public static final String UTF_8 = "UTF-8";

    /**
     * Create a RESTPubSub from a {@link BulletConfig}.
     *
     * @param config The config.
     * @throws PubSubException if the context name is not present or cannot be parsed.
     */
    public RESTPubSub(BulletConfig config) throws PubSubException {
        super(config);
        this.config = new RESTPubSubConfig(config);
    }

    @Override
    public Publisher getPublisher() {
        int connectTimeout = config.getAs(RESTPubSubConfig.PUBLISHER_CONNECT_TIMEOUT, Integer.class);
        if (context == Context.QUERY_PROCESSING) {
            return new RESTResultPublisher(HttpClients.createDefault(), connectTimeout);
        } else {
            String queryURL = ((List<String>) config.getAs(RESTPubSubConfig.QUERY_URLS, List.class)).get(0);
            String resultURL = config.getAs(RESTPubSubConfig.RESULT_URL, String.class);
            return new RESTQueryPublisher(HttpClients.createDefault(), queryURL, resultURL, connectTimeout);
        }
    }

    @Override
    public List<Publisher> getPublishers(int n) {
        return IntStream.range(0, n).mapToObj(i -> getPublisher()).collect(Collectors.toList());
    }

    @Override
    public Subscriber getSubscriber() {
        int maxUncommittedMessages = config.getAs(RESTPubSubConfig.MAX_UNCOMMITTED_MESSAGES, Integer.class);
        int connectTimeout = config.getAs(RESTPubSubConfig.SUBSCRIBER_CONNECT_TIMEOUT, Integer.class);
        List<String> urls;
        Long minWait;

        if (context == Context.QUERY_PROCESSING) {
            urls = (List<String>) config.getAs(RESTPubSubConfig.QUERY_URLS, List.class);
            minWait = config.getAs(RESTPubSubConfig.QUERY_SUBSCRIBER_MIN_WAIT, Long.class);
        } else {
            urls = Collections.singletonList(config.getAs(RESTPubSubConfig.RESULT_URL, String.class));
            minWait = config.getAs(RESTPubSubConfig.RESULT_SUBSCRIBER_MIN_WAIT, Long.class);
        }
        return new RESTSubscriber(maxUncommittedMessages, urls, HttpClients.createDefault(), minWait, connectTimeout);
    }

    @Override
    public List<Subscriber> getSubscribers(int n) {
        return IntStream.range(0, n).mapToObj(i -> getSubscriber()).collect(Collectors.toList());
    }
}
