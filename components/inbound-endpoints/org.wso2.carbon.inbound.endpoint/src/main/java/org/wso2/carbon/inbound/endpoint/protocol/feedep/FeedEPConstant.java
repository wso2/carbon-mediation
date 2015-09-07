/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.inbound.endpoint.protocol.feedep;

import javax.xml.namespace.QName;

class FeedEPConstant {

    public static final String FEED_URL = "feed.url";
    public static final String FEED_TYPE = "feed.type";
    public static final String FEED_TIMEFORMAT = "TimeFormat";
    public static final String FEED_LASTFEEDONLY = "LastFeedOnly";
    public static final String FEED_FORMAT = "TEXT";
    public static final String INBOUND_COORDINATION = "coordination";
    public static final String FEED_TYPE_RSS = "RSS";
    public static final String FEED_TYPE_ATOM = "Atom";

    private static final String RSS = "rss";
    private static final String CHANNEL = "channel";
    private static final String ITEM = "item";
    private static final String TITLE = "title";
    private static final String GUID = "guid";
    private static final String DESCRIPTION = "description";
    private static final String PUBDATE = "pubDate";
    private static final String LINK = "link";
    private static final String LASTBUILDDATE = "lastBuildDate";

    public static final String RSS_FEED_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
    public static final String REGISTRY_TIME_FORMAT = "E MMM dd HH:mm:ss Z yyyy";

    public static final QName FEED_RSS = new QName(RSS);
    public static final QName FEED_CHANNEL = new QName(CHANNEL);
    public static final QName FEED_ITEM = new QName(ITEM);
    public static final QName FEED_TITLE = new QName(TITLE);
    public static final QName FEED_GUID = new QName(GUID);
    public static final QName FEED_DESCRIPTION = new QName(DESCRIPTION);
    public static final QName FEED_PUBDATE = new QName(PUBDATE);
    public static final QName FEED_LINK = new QName(LINK);
    public static final QName FEED_LASTBUILDDATE = new QName(LASTBUILDDATE);
}
