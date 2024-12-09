package Mboussaid.laFactureFacile.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Mboussaid.laFactureFacile.Models.Items;

@Repository
public interface ItemsRepository extends JpaRepository<Items, Integer> {

}
