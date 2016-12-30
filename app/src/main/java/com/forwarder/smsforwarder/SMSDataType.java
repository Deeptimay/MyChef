package com.forwarder.smsforwarder;

/**
 * Created by Deeptimay on 1/4/2016.
 */
public class SMSDataType {

    //try
    //private variables
    int _id;
    String _body;
    String _phone_number;

    // Empty constructor
    public SMSDataType() {

    }

    // constructor
    public SMSDataType(int id, String body, String _phone_number) {
        this._id = id;
        this._body = body;
        this._phone_number = _phone_number;
    }

    // constructor
    public SMSDataType(String body, String _phone_number) {
        this._body = body;
        this._phone_number = _phone_number;
    }

    // getting ID
    public int getID() {
        return this._id;
    }

    // setting id
    public void setID(int id) {
        this._id = id;
    }

    // getting body
    public String getBody() {
        return this._body;
    }

    // setting body
    public void setBody(String body) {
        this._body = body;
    }

    // getting phone number
    public String getPhoneNumber() {
        return this._phone_number;
    }

    // setting phone number
    public void setPhoneNumber(String phone_number) {
        this._phone_number = phone_number;
    }
}