/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.alibaba.fastjson.JSONObject
 *  com.google.common.collect.Lists
 */
package rrd.bean;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import java.util.List;

public class RegionDetails {
    private String country = "1";
    private String country_name = "\u4e2d\u56fd";
    private String mobile = "";
    private String zipcode = "";
    private String consignee = "";
    private String province = "";
    private String province_name = "";
    private String city = "";
    private String city_name = "";
    private List<Integer> district = Lists.newArrayList();
    private String district_name = "";
    private String address = "";

    public JSONObject convertToJSON() {
        JSONObject jsonObject = new JSONObject();
        // \u53ea\u6709\u5f53 province\u3001city\u3001district \u5168\u4e3a\u7a7a\u65f6\u624d\u6e05\u7a7a\uff0c\u4fdd\u7559\u4efb\u4f55\u5df2\u89e3\u6790\u7684\u5730\u533a\u4fe1\u606f
        if (this.province.isEmpty() && this.city.isEmpty() && this.district.isEmpty()) {
            this.province = "";
            this.province_name = "";
            this.city = "";
            this.city_name = "";
            this.district_name = "";
        }
        jsonObject.put("country", (Object)1);
        jsonObject.put("country_name", (Object)"\u4e2d\u56fd");
        jsonObject.put("mobile", (Object)this.mobile);
        jsonObject.put("zipcode", (Object)this.zipcode);
        jsonObject.put("consignee", (Object)this.consignee);
        jsonObject.put("province", (Object)this.province);
        jsonObject.put("province_name", (Object)this.province_name);
        jsonObject.put("city", (Object)this.city);
        jsonObject.put("city_name", (Object)this.city_name);
        if (this.district.isEmpty()) {
            jsonObject.put("district", (Object)"");
        } else {
            jsonObject.put("district", (Object)this.district.get(0).toString());
        }
        jsonObject.put("district_name", (Object)this.district_name);
        jsonObject.put("address", (Object)this.address);
        return jsonObject;
    }

    public String getCountry() {
        return this.country;
    }

    public String getCountry_name() {
        return this.country_name;
    }

    public String getMobile() {
        return this.mobile;
    }

    public String getZipcode() {
        return this.zipcode;
    }

    public String getConsignee() {
        return this.consignee;
    }

    public String getProvince() {
        return this.province;
    }

    public String getProvince_name() {
        return this.province_name;
    }

    public String getCity() {
        return this.city;
    }

    public String getCity_name() {
        return this.city_name;
    }

    public List<Integer> getDistrict() {
        return this.district;
    }

    public String getDistrict_name() {
        return this.district_name;
    }

    public String getAddress() {
        return this.address;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCountry_name(String country_name) {
        this.country_name = country_name;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setProvince_name(String province_name) {
        this.province_name = province_name;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCity_name(String city_name) {
        this.city_name = city_name;
    }

    public void setDistrict(List<Integer> district) {
        this.district = district;
    }

    public void setDistrict_name(String district_name) {
        this.district_name = district_name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RegionDetails)) {
            return false;
        }
        RegionDetails other = (RegionDetails)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$country = this.getCountry();
        String other$country = other.getCountry();
        if (this$country == null ? other$country != null : !this$country.equals(other$country)) {
            return false;
        }
        String this$country_name = this.getCountry_name();
        String other$country_name = other.getCountry_name();
        if (this$country_name == null ? other$country_name != null : !this$country_name.equals(other$country_name)) {
            return false;
        }
        String this$mobile = this.getMobile();
        String other$mobile = other.getMobile();
        if (this$mobile == null ? other$mobile != null : !this$mobile.equals(other$mobile)) {
            return false;
        }
        String this$zipcode = this.getZipcode();
        String other$zipcode = other.getZipcode();
        if (this$zipcode == null ? other$zipcode != null : !this$zipcode.equals(other$zipcode)) {
            return false;
        }
        String this$consignee = this.getConsignee();
        String other$consignee = other.getConsignee();
        if (this$consignee == null ? other$consignee != null : !this$consignee.equals(other$consignee)) {
            return false;
        }
        String this$province = this.getProvince();
        String other$province = other.getProvince();
        if (this$province == null ? other$province != null : !this$province.equals(other$province)) {
            return false;
        }
        String this$province_name = this.getProvince_name();
        String other$province_name = other.getProvince_name();
        if (this$province_name == null ? other$province_name != null : !this$province_name.equals(other$province_name)) {
            return false;
        }
        String this$city = this.getCity();
        String other$city = other.getCity();
        if (this$city == null ? other$city != null : !this$city.equals(other$city)) {
            return false;
        }
        String this$city_name = this.getCity_name();
        String other$city_name = other.getCity_name();
        if (this$city_name == null ? other$city_name != null : !this$city_name.equals(other$city_name)) {
            return false;
        }
        List<Integer> this$district = this.getDistrict();
        List<Integer> other$district = other.getDistrict();
        if (this$district == null ? other$district != null : !((Object)this$district).equals(other$district)) {
            return false;
        }
        String this$district_name = this.getDistrict_name();
        String other$district_name = other.getDistrict_name();
        if (this$district_name == null ? other$district_name != null : !this$district_name.equals(other$district_name)) {
            return false;
        }
        String this$address = this.getAddress();
        String other$address = other.getAddress();
        return !(this$address == null ? other$address != null : !this$address.equals(other$address));
    }

    protected boolean canEqual(Object other) {
        return other instanceof RegionDetails;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $country = this.getCountry();
        result = result * 59 + ($country == null ? 43 : $country.hashCode());
        String $country_name = this.getCountry_name();
        result = result * 59 + ($country_name == null ? 43 : $country_name.hashCode());
        String $mobile = this.getMobile();
        result = result * 59 + ($mobile == null ? 43 : $mobile.hashCode());
        String $zipcode = this.getZipcode();
        result = result * 59 + ($zipcode == null ? 43 : $zipcode.hashCode());
        String $consignee = this.getConsignee();
        result = result * 59 + ($consignee == null ? 43 : $consignee.hashCode());
        String $province = this.getProvince();
        result = result * 59 + ($province == null ? 43 : $province.hashCode());
        String $province_name = this.getProvince_name();
        result = result * 59 + ($province_name == null ? 43 : $province_name.hashCode());
        String $city = this.getCity();
        result = result * 59 + ($city == null ? 43 : $city.hashCode());
        String $city_name = this.getCity_name();
        result = result * 59 + ($city_name == null ? 43 : $city_name.hashCode());
        List<Integer> $district = this.getDistrict();
        result = result * 59 + ($district == null ? 43 : ((Object)$district).hashCode());
        String $district_name = this.getDistrict_name();
        result = result * 59 + ($district_name == null ? 43 : $district_name.hashCode());
        String $address = this.getAddress();
        result = result * 59 + ($address == null ? 43 : $address.hashCode());
        return result;
    }

    public String toString() {
        return "RegionDetails(country=" + this.getCountry() + ", country_name=" + this.getCountry_name() + ", mobile=" + this.getMobile() + ", zipcode=" + this.getZipcode() + ", consignee=" + this.getConsignee() + ", province=" + this.getProvince() + ", province_name=" + this.getProvince_name() + ", city=" + this.getCity() + ", city_name=" + this.getCity_name() + ", district=" + this.getDistrict() + ", district_name=" + this.getDistrict_name() + ", address=" + this.getAddress() + ")";
    }
}
