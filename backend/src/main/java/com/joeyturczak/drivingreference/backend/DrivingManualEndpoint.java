package com.joeyturczak.drivingreference.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.config.Nullable;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.NotFoundException;

import com.joeyturczak.myjavalibrary.DrivingValues.Location;
import com.joeyturczak.myjavalibrary.DrivingValues.Type;
import com.joeyturczak.myjavalibrary.DrivingValues.Language;

import java.util.List;

import static com.joeyturczak.drivingreference.backend.ObjectifyStartupServlet.ofy;

/**
 * Copyright (C) 2016 Joey Turczak
 */

@Api(
        name = "drivingManualApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.drivingreference.joeyturczak.com",
                ownerName = "backend.drivingreference.joeyturczak.com",
                packagePath=""
        )
)
public class DrivingManualEndpoint {

    @ApiMethod(name = "insertDrivingManual")
    public DrivingManual insertDrivingManual(@Named("location")Location location, @Named("type")Type type,
                                             @Named("language")Language language, @Named("url")String url,
                                             @Named("displayName")String displayName) throws ConflictException {

        List<DrivingManual> drivingManualList = getDrivingManuals();

        for(DrivingManual drivingManual : drivingManualList) {
            Location checkLocation  = drivingManual.getLocation();
            Type checkType = drivingManual.getType();
            Language checkLanguage = drivingManual.getLanguage();
            if(location.equals(checkLocation) && type.equals(checkType) && language.equals(checkLanguage)) {
                throw new ConflictException("Driving manual already exists");
            }
        }

        DrivingManual newDrivingManual = new DrivingManual();
        newDrivingManual.setLocation(location);
        newDrivingManual.setType(type);
        newDrivingManual.setLanguage(language);
        newDrivingManual.setUrl(url);
        newDrivingManual.setDisplayName(displayName);

        long time = System.currentTimeMillis();

        newDrivingManual.setLastUpdated(time);

        saveDrivingManual(newDrivingManual);

        return newDrivingManual;
    }

    @ApiMethod(name = "modifyDrivingManual")
    public DrivingManual modifyDrivingManual(@Named("id") Long id, @Nullable @Named("location") Location location,
                                             @Nullable @Named("type") Type type, @Nullable @Named("language") Language language,
                                             @Nullable @Named("url") String url, @Nullable @Named("displayName") String displayName) throws NotFoundException {

        DrivingManual drivingManual = getDrivingManual(id);

        if(drivingManual == null) {
            throw new NotFoundException("Driving Manual does not exist");
        }

        if(location != null) {
            drivingManual.setLocation(location);
        }
        if(type != null) {
            drivingManual.setType(type);
        }
        if(language != null) {
            drivingManual.setLanguage(language);
        }
        if(url != null) {
            drivingManual.setUrl(url);
        }
        if(displayName != null) {
            drivingManual.setDisplayName(displayName);
        }

        long time = System.currentTimeMillis();

        drivingManual.setLastUpdated(time);

        saveDrivingManual(drivingManual);

        return drivingManual;
    }

    @ApiMethod(name = "removeHandBook")
    public void removeHandBook(@Named("id")Long id) throws NotFoundException {

        DrivingManual drivingManual = getDrivingManual(id);

        if(drivingManual == null) {
            throw new NotFoundException("Driving Manual does not exist");
        }

        ofy().delete().entity(drivingManual);
    }

    @ApiMethod(name = "getDrivingManuals")
    public List<DrivingManual> getDrivingManuals() {
        return ofy().load().type(DrivingManual.class).list();
    }

    @ApiMethod(name = "getDrivingManual")
    public DrivingManual getDrivingManual(@Named("id") Long id) {
        return ofy().load().type(DrivingManual.class).id(id).now();
    }

    @ApiMethod(name = "getDrivingManualsAfterDate")
    public List<DrivingManual> getDrivingManualsAfterDate(@Named("lastUpdated") long lastUpdated) {

        return ofy().load().type(DrivingManual.class).filter("lastUpdated >", lastUpdated).list();

    }

    @ApiMethod(name = "getDrivingManualWithoutId")
    public DrivingManual getDrivingManualWithoutId(@Named("location") Location location, @Named("type") Type type, @Named("language") Language language) throws NotFoundException {
        List<DrivingManual> drivingManualList = getDrivingManuals();

        for(DrivingManual drivingManual : drivingManualList) {
            Location checkLocation  = drivingManual.getLocation();
            Type checkType = drivingManual.getType();
            Language checkLanguage = drivingManual.getLanguage();
            if(location.equals(checkLocation) && type.equals(checkType) && language.equals(checkLanguage)) {
                return drivingManual;
            }
        }
        throw new NotFoundException("Driving Manual does not exist");
    }

    private void saveDrivingManual(DrivingManual drivingManual) {
        ofy().save().entity(drivingManual).now();
    }
}
