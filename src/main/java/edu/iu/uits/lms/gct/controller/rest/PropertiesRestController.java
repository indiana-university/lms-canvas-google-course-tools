package edu.iu.uits.lms.gct.controller.rest;

/*-
 * #%L
 * google-course-tools
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.gct.model.GctProperty;
import edu.iu.uits.lms.gct.repository.GctPropertyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by chmaurer on 5/15/20.
 */
@RestController
@RequestMapping("/rest/properties")
@Tag(name = "PropertiesRestController", description = "Interact with the GctProperty repository with CRUD operations")
@Slf4j
public class PropertiesRestController {

    @Autowired
    private GctPropertyRepository gctPropertyRepository = null;

    @GetMapping("/all")
    @Operation(summary = "Get all GctProperty objects")
    public Iterable<GctProperty> getAll() {
        return gctPropertyRepository.findAll();
    }

    @GetMapping("/{env}/{key}")
    @Operation(summary = "Get a GctProperty by env and key")
    public GctProperty getProperty(@PathVariable String env, @PathVariable String key) {
        return gctPropertyRepository.findByKeyAndEnv(key, env);
    }

    @PutMapping("/{env}/{key}")
    @Operation(summary = "Update a GctProperty by env and key")
    public GctProperty updateProperty(@PathVariable String env, @PathVariable String key, @RequestBody GctProperty gctProperty) {
        GctProperty prop = gctPropertyRepository.findByKeyAndEnv(key, env);

        if (gctProperty.getValue() != null) {
            prop.setValue(gctProperty.getValue());
        }

        if (gctProperty.getEnv() != null) {
            prop.setEnv(gctProperty.getEnv());
        }
        return gctPropertyRepository.save(prop);
    }

    @PostMapping("/")
    @Operation(summary = "Create a new GctProperty")
    public GctProperty createProperty(@RequestBody GctProperty gctProperty) {
        GctProperty newProp = new GctProperty(gctProperty.getKey(), gctProperty.getValue(), gctProperty.getEnv());
        return gctPropertyRepository.save(newProp);
    }

    @DeleteMapping("/{env}/{key}")
    @Operation(summary = "Delete a GctProperty by env and key")
    public String deleteProperty(@PathVariable String env, @PathVariable String key) {
        GctProperty prop = gctPropertyRepository.findByKeyAndEnv(key, env);
        gctPropertyRepository.delete(prop);
        return "Delete success.";
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a GctProperty by id")
    public String deletePropertyById(@PathVariable Long id) {
        gctPropertyRepository.deleteById(id);
        return "Delete success.";
    }
}
