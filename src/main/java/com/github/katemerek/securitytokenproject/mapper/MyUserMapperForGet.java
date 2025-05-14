package com.github.katemerek.securitytokenproject.mapper;

import com.github.katemerek.securitytokenproject.dto.MyUserDtoForGet;
import com.github.katemerek.securitytokenproject.model.MyUser;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MyUserMapperForGet {
    MyUser toEntity(MyUserDtoForGet myUserDtoForGet);

    MyUserDtoForGet toMyUserDtoForGet(MyUser myUser);
}