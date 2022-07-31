package com.pertalgad.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.Objects;
import java.util.Random;

@Getter
@Setter
public class Simple {
    private Long id;
    private String value;

    public Simple(){
        this.id = RandomUtils.nextLong(1,1000);
        this.value = RandomStringUtils.randomAlphabetic(10);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Simple that = (Simple) o;
        return getId().equals(that.getId()) && getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getValue());
    }
}
