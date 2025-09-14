package com.example.buildpro.repository;

import com.example.buildpro.model.Address;
import com.example.buildpro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);

    List<Address> findByUserAndIsDefaultTrue(User user);

    Optional<Address> findByIdAndUser(Long id, User user);

    void deleteByUser(User user);
}
