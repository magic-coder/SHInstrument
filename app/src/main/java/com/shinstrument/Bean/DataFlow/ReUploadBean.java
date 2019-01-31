package com.shinstrument.Bean.DataFlow;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ReUploadBean {

    @Id(autoincrement = true)
    private Long id;

    private String method;

    private byte[] content;

    private int type_patrol;

    @Generated(hash = 1441474913)
    public ReUploadBean(Long id, String method, byte[] content, int type_patrol) {
        this.id = id;
        this.method = method;
        this.content = content;
        this.type_patrol = type_patrol;
    }

    @Generated(hash = 1965321528)
    public ReUploadBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public byte[] getContent() {
        return this.content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public int getType_patrol() {
        return this.type_patrol;
    }

    public void setType_patrol(int type_patrol) {
        this.type_patrol = type_patrol;
    }
}
