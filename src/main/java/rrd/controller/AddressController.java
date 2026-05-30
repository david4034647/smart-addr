/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson.JSONObject
 *  org.apache.log4j.Logger
 *  org.springframework.beans.factory.annotation.Autowired
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.ResponseBody
 *  org.springframework.web.bind.annotation.RestController
 */
package rrd.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import rrd.bean.RegionDetails;
import rrd.service.AddressService;
import rrd.util.StringUtil;

@RestController
@RequestMapping(value={"/rrd"})
public class AddressController {
    private static Logger logger = Logger.getLogger(AddressController.class);
    @Autowired
    private AddressService addressService;

    @RequestMapping(value={"/address/extraction"})
    @ResponseBody
    public JSONObject testQuery(@RequestParam(value="s") String s) {
        long systemTime = System.nanoTime();
        s = StringUtil.toSegWordsStand(s);
        logger.info((Object)("id=" + systemTime + ",input = " + s));
        JSONObject jsonObject = new JSONObject();
        try {
            if (s == null || s.isEmpty()) {
                return new RegionDetails().convertToJSON();
            }
            jsonObject = this.addressService.getWords(s);
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error((Object)"\u5730\u5740\u83b7\u53d6\u5931\u8d25", (Throwable)e);
        }
        logger.info((Object)("id=" + systemTime + ",output=" + jsonObject.toJSONString() + ",time=" + (System.nanoTime() - systemTime)));
        return jsonObject;
    }
}
