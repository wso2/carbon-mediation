/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.mediation.dependency.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the data structure which acts as the dependency tree for Synapse
 * configuration items. It keeps track of configuration objects (sequences, endpoints etc)
 * and the inter dependencies among them. In effect this functions as a directed graph
 * which consists of a set of vertices and a set of directed edges. A vertex represents a
 * unique configuration item and an edge from vertex 'A' to vertex 'B' indicates that item
 * 'A' is a dependency for 'B'.
 */    
class DependencyGraph {

    private static final Log log = LogFactory.getLog(DependencyGraph.class);

    private final Set<ConfigurationObject> vertices = new HashSet<ConfigurationObject>();

    private final Set<DirectedEdge> edges = new HashSet<DirectedEdge>();

    public boolean add(ConfigurationObject o) {
        if (o == null) {
            throw new NullPointerException("null objects cannot be added to the dependency graph");
        }

        return vertices.add(o);
    }

    public boolean remove(int type, String id) {
        ConfigurationObject o = find(type, id);
        if (o != null) {
            if (hasDependents(o)) {
                // If this object has any dependents we mark it as unknown
                // This object will be resolved when we add an item by the same name
                o.setType(ConfigurationObject.TYPE_UNKNOWN);
            } else {
                // This is an orphan - with no dependents
                // So simply remove it - Nobody should care
                remove(o);
            }
        }
        return false;
    }

    private boolean remove(ConfigurationObject o) {
        if (vertices.remove(o)) {
            List<DirectedEdge> deletedEdges = new ArrayList<DirectedEdge>();

            for (DirectedEdge e : edges) {
                if (e.getStart() == o || e.getEnd() == o) {
                    deletedEdges.add(e);
                }
            }

            for (DirectedEdge e : deletedEdges) {
                edges.remove(e);
            }

            // Deleting a vertex may introduce some orphan unknown objects
            // We need to remove them from the graph
            cleanupUnknownObjects();
            return true;
        }
        return false;
    }

    private void cleanupUnknownObjects() {
        List<ConfigurationObject> deletedVertices = new ArrayList<ConfigurationObject>();
        for (ConfigurationObject v : vertices) {
            if (v.getType() == ConfigurationObject.TYPE_UNKNOWN) {
                boolean edgeFound = false;
                for (DirectedEdge e : edges) {
                    if (e.getStart() == v || e.getEnd() == v) {
                        edgeFound = true;
                        break;
                    }
                }

                // If the vertex is of the type 'UNKNOWN' and is not connected to any edges
                // there is no point in keeping it any longer....
                if (!edgeFound) {
                    deletedVertices.add(v);
                }
            }
        }

        for (ConfigurationObject v : deletedVertices) {
            vertices.remove(v);
        }
    }

    public boolean createEdge(ConfigurationObject start, ConfigurationObject end) {
		add(start);
		add(end);
		for (DirectedEdge e : edges) {
			if (e.getStart() == start && e.getEnd() == end) {
				return false;
			}
		}

		edges.add(new DirectedEdge(start, end));
        if (log.isDebugEnabled()) {
            log.debug("Dependency detected: {" + start.getTypeName() + ":" + start.getId() +
                    "}" + " ---> {" + end.getTypeName() + ":" + end.getId() +
                    "}, Tracking...");
        }
		return true;
	}

    public ConfigurationObject find(int type, String Id) {
        for (ConfigurationObject o : vertices) {
			if (o.getId() !=null) {   //default type doesnt have ID
				if (o.getType() == type && o.getId() != null && o.getId().equals(Id)) {
					return o;
				}
			}
        }
        return null;
    }

    public boolean hasDependents(ConfigurationObject o) {
        for (DirectedEdge e : edges) {
            if (e.getStart() == o) {
                return true;
            }
        }
        return false;
    }

    public boolean hasActiveDependents(ConfigurationObject o) {
        for (DirectedEdge e : edges) {
            if (e.getStart() == o && e.getEnd().getType() != ConfigurationObject.TYPE_UNKNOWN) {
                return true;
            }
        }
        return false;
    }

    public ConfigurationObject[] getDependents(ConfigurationObject o) {
        List<ConfigurationObject> dependents = new ArrayList<ConfigurationObject>();
        for (DirectedEdge e : edges) {
            if (e.getStart() == o) {
                dependents.add(e.getEnd());
            }
        }

        if (dependents.size() > 0) {
            return dependents.toArray(new ConfigurationObject[dependents.size()]);
        }
        return null;
    }

    public void resolveObject(ConfigurationObject o, int type) {
        o.setType(type);
        if (log.isDebugEnabled()) {
            log.debug("Configuration object resolved: key : " + o.getId() + ", type : " +
                    o.getTypeName());
        }
    }

    public void removeDependencies(ConfigurationObject o) {
        List<DirectedEdge> dependencies = new ArrayList<DirectedEdge>();
        for (DirectedEdge e : edges) {
            if (e.getEnd() == o) {
                dependencies.add(e);
            }
        }

        for (DirectedEdge e : dependencies) {
            edges.remove(e);
        }
    }
}
