package com.example.demo.repository;

import com.example.demo.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findAllByType(String type);

    Optional<Employee> findByIdAndType(Integer id, String type);

    boolean existsByIdAndType(Integer id, String type);

    void deleteByIdAndType(Integer id, String type);

    @Query("select distinct e from Employee e, Trip t where t.vehicleId = :vehicleId "
            + "and t.driverId = e.id and e.type = :type")
    List<Employee> findDistinctAssignedToVehicle(
            @Param("vehicleId") Integer vehicleId,
            @Param("type") String type
    );
}
