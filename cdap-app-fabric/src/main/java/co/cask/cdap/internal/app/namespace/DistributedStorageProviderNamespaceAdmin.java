/*
 * Copyright © 2016 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.internal.app.namespace;

import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.namespace.NamespaceQueryAdmin;
import co.cask.cdap.common.namespace.NamespacedLocationFactory;
import co.cask.cdap.data2.util.hbase.HBaseDDLExecutorFactory;
import co.cask.cdap.data2.util.hbase.HBaseTableUtil;
import co.cask.cdap.explore.client.ExploreFacade;
import co.cask.cdap.explore.service.ExploreException;
import co.cask.cdap.proto.NamespaceConfig;
import co.cask.cdap.proto.NamespaceMeta;
import co.cask.cdap.proto.id.NamespaceId;
import co.cask.cdap.spi.hbase.HBaseDDLExecutor;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Manages namespaces on underlying systems - HDFS, HBase, Hive, etc.
 */
public final class DistributedStorageProviderNamespaceAdmin extends AbstractStorageProviderNamespaceAdmin {

  private static final Logger LOG = LoggerFactory.getLogger(DistributedStorageProviderNamespaceAdmin.class);

  private final Configuration hConf;
  private final HBaseTableUtil tableUtil;
  private final NamespaceQueryAdmin namespaceQueryAdmin;
  private final HBaseDDLExecutorFactory hBaseDDLExecutorFactory;

  @Inject
  DistributedStorageProviderNamespaceAdmin(CConfiguration cConf,
                                           NamespacedLocationFactory namespacedLocationFactory,
                                           ExploreFacade exploreFacade, HBaseTableUtil tableUtil,
                                           NamespaceQueryAdmin namespaceQueryAdmin) {
    super(cConf, namespacedLocationFactory, exploreFacade, namespaceQueryAdmin);
    this.hConf = HBaseConfiguration.create();
    this.tableUtil = tableUtil;
    this.namespaceQueryAdmin = namespaceQueryAdmin;
    this.hBaseDDLExecutorFactory = new HBaseDDLExecutorFactory(cConf, hConf);
  }

  @Override
  public void create(NamespaceMeta namespaceMeta) throws IOException, ExploreException, SQLException {
    // create filesystem directory
    super.create(namespaceMeta);
    // skip namespace creation in HBase for default namespace
    if (NamespaceId.DEFAULT.equals(namespaceMeta.getNamespaceId())) {
      return;
    }
    // create HBase namespace and set group C(reate) permission if a group is configured
    String hbaseNamespace = tableUtil.getHBaseNamespace(namespaceMeta);

    if (Strings.isNullOrEmpty(namespaceMeta.getConfig().getHbaseNamespace())) {
      try (HBaseDDLExecutor executor = hBaseDDLExecutorFactory.get()) {
        boolean created = executor.createNamespaceIfNotExists(hbaseNamespace);
        if (namespaceMeta.getConfig().getGroupName() != null) {
          try {
            executor.grantPermissions(hbaseNamespace, null,
                                      ImmutableMap.of("@" + namespaceMeta.getConfig().getGroupName(), "C"));
          } catch (IOException | RuntimeException e) {
            // don't leave a partial state behind, as this fails the create(), the namespace should be removed
            if (created) {
              try {
                executor.deleteNamespaceIfExists(hbaseNamespace);
              } catch (Throwable t) {
                e.addSuppressed(t);
              }
            }
            throw e;
          }
        }
      } catch (Throwable t) {
        try {
          // if we failed to create a namespace in hbase then do clean up for above creations
          super.delete(namespaceMeta.getNamespaceId());
        } catch (Exception e) {
          t.addSuppressed(e);
        }
        throw t;
      }
    }

    try (HBaseAdmin admin = new HBaseAdmin(hConf)) {
      if (!tableUtil.hasNamespace(admin, hbaseNamespace)) {
        throw new IOException(String.format("Custom mapped HBase namespace doesn't exist %s for namespace %s",
                                            hbaseNamespace, namespaceMeta.getName()));
      }
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void delete(NamespaceId namespaceId) throws IOException, ExploreException, SQLException {
    // delete namespace directory from filesystem
    super.delete(namespaceId);
    if (NamespaceId.DEFAULT.equals(namespaceId)) {
      return;
    }
    // delete HBase namespace
    NamespaceConfig namespaceConfig;
    try {
      namespaceConfig = namespaceQueryAdmin.get(namespaceId).getConfig();
    } catch (Exception ex) {
      throw new IOException("Could not fetch custom HBase mapping.", ex);
    }

    if (!Strings.isNullOrEmpty(namespaceConfig.getHbaseNamespace())) {
      // custom namespace mapping is set for HBase, hence don't do anything during delete since the lifecycle of the
      // namespace will be managed by the user
      LOG.debug("Custom HBase mapping {} was found while deleting {}. Hence skipping deletion of HBase namespace",
                namespaceConfig.getHbaseNamespace(), namespaceId);
      return;
    }
    // delete HBase namespace
    String namespace = tableUtil.getHBaseNamespace(namespaceId);
    try (HBaseDDLExecutor executor = hBaseDDLExecutorFactory.get()) {
      executor.deleteNamespaceIfExists(namespace);
    }
  }
}
