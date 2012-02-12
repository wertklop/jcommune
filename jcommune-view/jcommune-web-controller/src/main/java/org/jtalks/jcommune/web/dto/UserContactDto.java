package org.jtalks.jcommune.web.dto;

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

import org.jtalks.jcommune.model.entity.UserContact;
import org.jtalks.jcommune.model.entity.UserContactType;

/**
 * Dto for transferring user contacts to client side.
 * @author Michael Gamov
  */
public class UserContactDto {

    private Long id;
    private Long ownerId;
    private String value;
    private UserContactType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UserContactType getType() {
        return type;
    }

    public void setType(UserContactType type) {
        this.type = type;
    }

    public static UserContactDto getDtoFor(UserContact contact) {
        UserContactDto dto = new UserContactDto();
        dto.setId(contact.getId());
        dto.setOwnerId(contact.getOwner().getId());
        dto.setValue(contact.getValue());
        dto.setType(contact.getType());
        return dto;
    }

}
