package com.stuben.evenx.evenx;

import com.stuben.evenx.protocol.BaseEvenx;

public class LocalEvenx extends BaseEvenx {
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
