package hr.datastock.services;

import hr.datastock.entities.StavkaIzdatniceEntity;

import java.util.List;
import java.util.Optional;

public interface StavkaIzdatniceService {

    List<StavkaIzdatniceEntity> getAll();

    StavkaIzdatniceEntity createStavkaIzdatnice(final StavkaIzdatniceEntity izdatnica);

    Optional<StavkaIzdatniceEntity> createStornoStavkeIzdatnice(final StavkaIzdatniceEntity updateStavke);
}
