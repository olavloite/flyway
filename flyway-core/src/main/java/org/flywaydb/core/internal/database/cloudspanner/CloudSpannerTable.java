/*
 * Copyright 2010-2017 Boxfuse GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flywaydb.core.internal.database.cloudspanner;

import org.flywaydb.core.internal.database.Database;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.jdbc.JdbcTemplate;
import org.flywaydb.core.internal.database.Schema;
import org.flywaydb.core.internal.database.Table;
import org.flywaydb.core.api.logging.Log;
import org.flywaydb.core.api.logging.LogFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Google Cloud Spanner-specific table.
 */
public class CloudSpannerTable extends Table {
    private static final Log LOG = LogFactory.getLog(CloudSpannerTable.class);

    /**
     * Creates a new Google Cloud Spanner table.
     *
     * @param jdbcTemplate The Jdbc Template for communicating with the DB.
     * @param database     The database-specific support.
     * @param schema       The schema this table lives in.
     * @param name         The name of the table.
     */
    public CloudSpannerTable(JdbcTemplate jdbcTemplate, Database database, Schema schema, String name) {
        super(jdbcTemplate, database, schema, name);
    }

    @Override
    protected void doDrop() throws SQLException {
    	try(ResultSet rs = jdbcTemplate.getConnection().getMetaData().getIndexInfo("", "", name, false, false)) {
    		while(rs.next()) {
    			jdbcTemplate.execute("DROP INDEX " + database.quote(rs.getString("INDEX_NAME")));
    		}
    	}
        jdbcTemplate.execute("DROP TABLE " + database.quote(name));
    }

    @Override
    protected boolean doExists() throws SQLException {
    	try(ResultSet rs = jdbcTemplate.getConnection().getMetaData().getTables("", "", name, null)) {
    		return rs.next();
    	}
    }

    @Override
    protected void doLock() throws SQLException {
    	jdbcTemplate.queryForInt("SELECT COUNT(*) FROM " + this);
    }
    
    @Override
    public String toString() {
    	if(StringUtils.hasLength(schema.getName()))
    		return database.quote(schema.getName(), name);
    	return database.quote(name);
    }
}