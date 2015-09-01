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

package org.wso2.carbon.inbound.endpoint.protocol.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.namespace.QName;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.filter.ListParseFilter;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.ParserOptions;
import org.apache.abdera.util.Constants;
import org.apache.abdera.util.filter.WhiteListParseFilter;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.inbound.endpoint.common.InboundRequestProcessorImpl;

/**
 * ConsumeFeed uses to feeds from given Backend
 */

public class ConsumeFeed extends InboundRequestProcessorImpl {

	private static final Log log = LogFactory.getLog(ConsumeFeed.class.getName());
	private long scanInterval;
	private long lastRanTime;
	private String host;
	private String feedType;
	private Feed feed = null;
	private OMElement item = null;
	Date date;
	private java.util.Date LastUpdated;;
	RssInject rssInject;
	Document<Feed> doc;
	InputStream input;
	DateFormat format;
	Parser parser;
	ParserOptions opts;
	ListParseFilter filter;
	Entry entry = null;
	Factory factory;
	RegistryHandler registryHandler;
	String PathName;

	public ConsumeFeed(RssInject rssInject, long scanInterval, String host, String feedType,
	                   RegistryHandler registryHandler, String name) {
		this.host = host;
		this.feedType = feedType;
		this.scanInterval = scanInterval;
		this.rssInject = rssInject;
		this.registryHandler = registryHandler;
		this.PathName = name;
	}

	public void execute() {
		try {
			long currentTime = (new Date()).getTime();
			if (((lastRanTime + scanInterval) <= currentTime)) {
				lastRanTime = currentTime;
				log.debug("lastRanTime " + lastRanTime);
				consume();
			} else if (log.isDebugEnabled()) {
				log.debug("Skip cycle since concurrent rate is higher than the scan interval : Feed Inbound EP ");
			}
			if (log.isDebugEnabled()) {
				log.debug("End : Feed Inbound EP : ");
			}
		} catch (Exception e) {
			log.error("Error while retrieving or injecting RSS message." + e.getMessage(), e);
		}
	}

	public void consume() throws ClassNotFoundException, IOException {

		format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

		parser = Abdera.getNewParser();
		try {
			input = new URL(host).openStream();
		} catch (Exception e) {
			log.error("Error while retrieving RSS message." + e.getMessage());
			return;
		}

		opts = parser.getDefaultParserOptions();
		filter = new WhiteListParseFilter();
		try {
			if (feedType.equalsIgnoreCase("RSS")) {
				filter.add(new QName("rss"));
				filter.add(new QName("channel"));
				filter.add(new QName("item"));
				filter.add(new QName("title"));
				filter.add(new QName("guid"));
				filter.add(new QName("description"));
				filter.add(new QName("pubDate"));
			} else if (feedType.equalsIgnoreCase("Atom")) {
				filter.add(Constants.FEED);
				filter.add(Constants.ENTRY);
				filter.add(Constants.TITLE);
				filter.add(Constants.ID);
				filter.add(Constants.CONTENT);
				filter.add(Constants.UPDATED);
				filter.add(Constants.LINK);
				filter.add(Constants.AUTHOR);
			}
		} catch (Exception e) {
			log.error("error with feed type." + e.getMessage());
			return;
		}

		opts.setParseFilter(filter);

		try {
			doc = parser.parse(input, "", opts);
			if (doc.getRoot() == null) {
				log.error("Please Check Host Address or Feed Type");
				return;
			}
			try {
				log.debug(doc.getXmlVersion());
			} catch (Exception e) {
				log.error(e.getMessage());
				return;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			return;
		}

		try {
			if (feedType.equalsIgnoreCase("RSS")) {
				factory = Abdera.getNewFactory();
				feed = factory.newFeed();

				item = (OMElement) doc.getRoot();

				@SuppressWarnings("rawtypes")
				Iterator values1 = item.getFirstElement().getChildrenWithName(new QName("item"));
				while (values1.hasNext()) {
					entry = feed.insertEntry();
					OMElement omElement = (OMElement) values1.next();

					@SuppressWarnings("rawtypes")
					Iterator values2 = omElement.getChildrenWithName(new QName("title"));
					while (values2.hasNext()) {
						OMElement Title = (OMElement) values2.next();
						entry.setTitle(Title.getText());
					}

					@SuppressWarnings("rawtypes")
					Iterator values3 = omElement.getChildrenWithName(new QName("pubDate"));
					while (values3.hasNext()) {
						OMElement Updated = (OMElement) values3.next();
						try {
							date = format.parse(Updated.getText());
							entry.setUpdated(date);
						} catch (ParseException e) {
							format =
							         new SimpleDateFormat("EEE, d MMM yyyy HH:mm Z", Locale.ENGLISH);
							date = format.parse(Updated.getText());
							entry.setUpdated(date);
						}
					}

					@SuppressWarnings("rawtypes")
					Iterator values4 = omElement.getChildrenWithName(new QName("description"));
					while (values4.hasNext()) {
						OMElement Content = (OMElement) values4.next();
						entry.setContent(Content.getText());
					}

					@SuppressWarnings("rawtypes")
					Iterator guid = omElement.getChildrenWithName(new QName("guid"));
					while (guid.hasNext()) {
						OMElement guid1 = (OMElement) guid.next();
						entry.setContent(guid1.getText());
					}
				}
			}

			else if (feedType.equalsIgnoreCase("Atom")) {
				feed = doc.getRoot();
			}
			try {
				log.debug(LastUpdated + " : " + feed.getEntries().get(0).getUpdated());
				format = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
				if (registryHandler.readFromRegistry(PathName) != null) {
					LastUpdated =
					              format.parse(registryHandler.readFromRegistry(PathName)
					                                          .toString());
				} else {
					registryHandler.writeToRegistry(PathName, feed.getEntries().get(0).getUpdated());
				}
				if (LastUpdated == null) {
					rssInject.invoke(feed);
					registryHandler.writeToRegistry(PathName, feed.getEntries().get(0).getUpdated());
					log.debug("LastUpdated Date was Null");
				} else if (feed.getEntries().get(0).getUpdated().after(LastUpdated)) {
					rssInject.invoke(feed);
					registryHandler.writeToRegistry(PathName, feed.getEntries().get(0).getUpdated());
					log.debug("New Entry was Added");
				} else {
					log.debug("there Is No New Feed");
					return;
				}

			} catch (Exception e) {
				log.error(e.getMessage());
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		input.close();
	}

	@Override
	public void init() {
	}
}
