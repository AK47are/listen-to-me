package com.github.listen_to_me.service;

import com.github.listen_to_me.domain.dto.LoginDTO;
import com.github.listen_to_me.domain.vo.LoginVO;

public interface AuthService {

    LoginVO loginUser(LoginDTO loginDTO);

    LoginVO refreshToken();
}
