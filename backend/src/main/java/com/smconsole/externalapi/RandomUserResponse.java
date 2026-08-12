package com.smconsole.externalapi;

import java.util.List;

public record RandomUserResponse(
        List<Result> results
) {
    public record Result(
            Name name,
            String email,
            String phone
    ) {
        public record Name(
                String first,
                String last
        ) {}
    }
}
