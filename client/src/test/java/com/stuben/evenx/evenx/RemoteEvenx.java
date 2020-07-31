package com.stuben.evenx.evenx;

import com.stuben.evenx.protocol.BaseEvenx;
import com.stuben.evenx.protocol.EvenxDeclare;

@EvenxDeclare(share = true)
public class RemoteEvenx extends BaseEvenx {
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
