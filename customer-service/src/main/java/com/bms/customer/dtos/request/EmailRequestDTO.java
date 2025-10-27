package com.bms.customer.dtos.request;

import lombok.Builder;

@Builder
public class EmailRequestDTO {
    private String toEmail;
    private String customerName;
    private String cifId;

    public EmailRequestDTO(){}

    public EmailRequestDTO(String toEmail, String customerName, String cifId) {
        this.toEmail = toEmail;
        this.customerName = customerName;
        this.cifId = cifId;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCifId() {
        return cifId;
    }

    public void setCifId(String cifId) {
        this.cifId = cifId;
    }

    @Override
    public String toString() {
        return "EmailRequestDTO{" +
                "toEmail='" + toEmail + '\'' +
                ", customerName='" + customerName + '\'' +
                ", cifId='" + cifId + '\'' +
                '}';
    }
}

