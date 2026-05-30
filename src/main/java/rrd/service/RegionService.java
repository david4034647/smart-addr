/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.stereotype.Service
 */
package rrd.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rrd.bean.Region;
import rrd.dao.RegionDao;

@Service
public class RegionService {
    @Autowired
    private RegionDao regionDao;

    public List<Region> findAllRegion() {
        return this.regionDao.findAllRegion();
    }

    public Region findRegionById(int id) {
        return this.regionDao.findRegionById(id);
    }
}
