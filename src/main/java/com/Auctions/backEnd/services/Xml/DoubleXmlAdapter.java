package com.Auctions.backEnd.services.Xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DoubleXmlAdapter extends XmlAdapter<String, Double>{

    @Override
    public String marshal(Double d) throws Exception {
        if (d != null){
            return d.toString();
        }
        else {
            return null;
        }
    }

    @Override
    public Double unmarshal(String s) throws Exception {
        try {
            return Double.valueOf(s.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
