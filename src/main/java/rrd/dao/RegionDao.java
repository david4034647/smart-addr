/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.ibatis.annotations.Param
 *  org.apache.ibatis.annotations.Select
 */
package rrd.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import rrd.bean.Region;

public interface RegionDao {
    @Select(value={"select id as regionId,name as regionName, parent_id as parRegionId from wxrrd.region"})
    public List<Region> findAllRegion();

    @Select(value={"select id as regionId,name as regionName from wxrrd.region where id = #{id}"})
    public Region findRegionById(@Param(value="id") int var1);

    @Select(value={"select id,name from wxrrd.region where region = #{region}"})
    public Region findRegionByRegionName(@Param(value="region") String var1);
}
