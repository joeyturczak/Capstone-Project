package com.joeyturczak.drivingreference.backend;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * Copyright (C) 2016 Joey Turczak
 */
public class ObjectifyStartupServlet {

    static {
        ObjectifyService.register(DrivingManual.class);
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
