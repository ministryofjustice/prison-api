
package net.syscon.elite.web.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Returns the list of Agencies
 * (Generated with springmvc-raml-parser v.0.8.9)
 * 
 */
@RestController
@RequestMapping(value = "/api/agencies", produces = "application/json")
public interface AgencyController {


    /**
     * No description
     * 
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getAgencies(
        @RequestParam(required = false, defaultValue = "2")
        String limit,
        @RequestParam(required = false, defaultValue = "0")
        String offset,
        @RequestHeader(name = "ConfigDataHeader")
        String configDataHeader);

    /**
     * No description
     * 
     */
    @RequestMapping(value = "/{startingWith}", method = RequestMethod.GET)
    public ResponseEntity<?> getAgencyByStartingWith(
        @PathVariable
        String startingWith,
        @RequestParam(required = false, defaultValue = "2")
        String limit,
        @RequestParam(required = false, defaultValue = "0")
        String offset,
        @RequestHeader(name = "ConfigDataHeader")
        String configDataHeader);

}
