/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jtalks.jcommune.web.controller;

import org.jtalks.common.model.entity.Component;
import org.jtalks.common.model.entity.ComponentType;
import org.jtalks.jcommune.model.entity.ComponentInformation;
import org.jtalks.jcommune.service.ComponentService;
import org.jtalks.jcommune.service.exceptions.ImageProcessException;
import org.jtalks.jcommune.service.nontransactional.AvatarService;
import org.jtalks.jcommune.service.nontransactional.Base64Wrapper;
import org.jtalks.jcommune.service.nontransactional.ForumLogoService;
import org.jtalks.jcommune.web.dto.json.JsonResponse;
import org.jtalks.jcommune.web.dto.json.JsonResponseStatus;
import org.jtalks.jcommune.web.util.ImageControllerUtils;
import org.jtalks.jcommune.web.util.JSONUtils;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;

import static org.jgroups.util.Util.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Andrei Alikov
 */
public class AdministrationControllerTest {
    private byte[] validImage = new byte[] {-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0,
            0, 0, 4, 0, 0, 0, 4, 1, 0, 0, 0, 0, -127, -118, -93, -45, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 1,
            -118, 0, 0, 1, -118, 1, 51, -105, 48, 88, 0, 0, 0, 32, 99, 72, 82, 77, 0, 0, 122, 37, 0, 0,
            -128, -125, 0, 0, -7, -1, 0, 0, -128, -23, 0, 0, 117, 48, 0, 0, -22, 96, 0, 0, 58, -104, 0, 0,
            23, 111, -110, 95, -59, 70, 0, 0, 0, 22, 73, 68, 65, 84, 120, -38, 98, -40, -49, -60, -64, -92,
            -64, -60, 0, 0, 0, 0, -1, -1, 3, 0, 5, -71, 0, -26, -35, -7, 32, 96, 0, 0, 0, 0, 73, 69, 78, 68,
            -82, 66, 96, -126
    };
    private static final String IMAGE_BYTE_ARRAY_IN_BASE_64_STRING = "it's dummy string";

    @Mock
    ComponentService componentService;

    @Mock
    MessageSource messageSource;

    @Mock
    ForumLogoService forumLogoService;

    @Mock
    ImageControllerUtils imageControllerUtils;
    //
    private AdministrationController administrationController;

    @BeforeMethod
    public void init() {
        initMocks(this);

        Component component = new Component("Forum", "Cool Forum", ComponentType.FORUM);
        component.setId(42);

        administrationController = new AdministrationController(componentService, imageControllerUtils,
                                                    messageSource, forumLogoService);
    }

    @Test
    public void enterAdminModeShouldSetSessionAttributeAndReturnPreviousPageRedirect() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String initialPage = "/topics/2";
        when(request.getHeader("Referer")).thenReturn(initialPage);
        HttpSession session = new MockHttpSession();
        when(request.getSession()).thenReturn(session);

        String resultUrl = administrationController.enterAdministrationMode(request);

        Boolean attr = (Boolean)session.getAttribute(AdministrationController.ADMIN_ATTRIBUTE_NAME);
        assertTrue(attr);
        assertEquals(resultUrl, "redirect:" + initialPage);
    }

    @Test
    public void exitAdminModeShouldRemoveSessionAttributeAndReturnPreviousPageRedirect() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String initialPage = "/topics/2";
        when(request.getHeader("Referer")).thenReturn(initialPage);
        HttpSession session = new MockHttpSession();
        when(request.getSession()).thenReturn(session);

        String resultUrl = administrationController.exitAdministrationMode(request);

        Object attr = session.getAttribute(AdministrationController.ADMIN_ATTRIBUTE_NAME);

        assertNull(attr);
        assertEquals(resultUrl, "redirect:" + initialPage);
    }

    @Test
    public void validForumInformationShouldProduceSuccessResponse() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "");
        ComponentInformation ci = new ComponentInformation();
        JsonResponse response = administrationController.setForumInformation(ci, bindingResult);

        verify(componentService).setComponentInformation(ci);
        assertEquals(response.getStatus(), JsonResponseStatus.SUCCESS);
    }

    @Test
    public void invalidForumInformationShouldProduceFailResponse() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "");
        bindingResult.addError(new ObjectError("name", "message"));
        JsonResponse response = administrationController.setForumInformation(new ComponentInformation(), bindingResult);

        assertEquals(response.getStatus(), JsonResponseStatus.FAIL);
    }

    @Test
    public void uploadLogoForOperaAndIEShouldReturnPreviewInResponce()
            throws IOException, ImageProcessException {
        MultipartFile file = new MockMultipartFile("qqfile", validImage);

        ResponseEntity<String> actualResponseEntity = administrationController.uploadLogo(file);

        verify(imageControllerUtils).prepareResponse(eq(file), any(HttpHeaders.class), any(HashMap.class));
    }

    @Test
    public void uploadLogoForChromeAndFFShouldReturnPreviewInResponce() throws ImageProcessException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        administrationController.uploadLogo(validImage, response);

        verify(imageControllerUtils).prepareResponse(eq(validImage), eq(response), any(HashMap.class));
    }

    @Test
    public void getDefaultLogoShouldReturnDefaultAvatarInBase64String() throws IOException, ImageProcessException {
        String expectedJSON = "{\"team\": \"larks\"}";
        when(forumLogoService.getDefaultLogo()).thenReturn(validImage);
        when(forumLogoService.convertBytesToBase64String(validImage)).thenReturn(IMAGE_BYTE_ARRAY_IN_BASE_64_STRING);
        when(imageControllerUtils.getResponceJSONString(Matchers.anyMap())).thenReturn(expectedJSON);

        String actualJSON = administrationController.getDefaultLogoInJson();

        verify(forumLogoService).getDefaultLogo();
        assertEquals(actualJSON, expectedJSON);
    }

    @Test
    public void getForumLogoShouldRetrunDefaultLogoWhenLogoPropertyIsEmpty() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        Component forumComponent = new Component();
        forumComponent.addProperty(AdministrationController.JCOMMUNE_LOGO_PARAM, "");
        when(componentService.getComponentOfForum()).thenReturn(forumComponent);
        when(forumLogoService.getDefaultLogo()).thenReturn(new byte[0]);

        administrationController.getForumLogo(response);

        verify(forumLogoService).getDefaultLogo();
    }

    @Test
    public void getForumLogoShouldRetrunDefaultLogoWhenLogoPropertyIsNull() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        Component forumComponent = new Component();
        when(componentService.getComponentOfForum()).thenReturn(forumComponent);
        when(forumLogoService.getDefaultLogo()).thenReturn(new byte[0]);

        administrationController.getForumLogo(response);

        verify(forumLogoService).getDefaultLogo();
    }

    @Test
    public void getForumLogoShouldRetrunDefaultLogoWhenNoComponent() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(componentService.getComponentOfForum()).thenReturn(null);
        when(forumLogoService.getDefaultLogo()).thenReturn(new byte[0]);

        administrationController.getForumLogo(response);

        verify(forumLogoService).getDefaultLogo();
    }

    @Test
    public void getForumLogoShouldRetrunDefaultLogoWhenPropertyExists() throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();

        Component forumComponent = new Component();
        String logo = "logo";
        Base64Wrapper wrapper = new Base64Wrapper();
        byte[] logoBytes = wrapper.decodeB64Bytes(logo);

        forumComponent.addProperty(AdministrationController.JCOMMUNE_LOGO_PARAM, logo);
        when(componentService.getComponentOfForum()).thenReturn(forumComponent);

        administrationController.getForumLogo(response);

        assertEquals(response.getContentAsByteArray(), logoBytes);
        verify(forumLogoService, never()).getDefaultLogo();
    }
}
