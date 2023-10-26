package com.anzi.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author anzi
 */
@AllArgsConstructor
@Getter
public enum PackageType {

    REQUEST_PACK(0),
    RESPONSE_PACK(1);

    private final int code;

}
