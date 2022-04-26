package hr.datastock.controllers.service.impl;

import hr.datastock.controllers.IzdatnicaController;
import hr.datastock.controllers.dialogutil.UtilService;
import hr.datastock.controllers.service.IzdatnicaControllerService;
import hr.datastock.entities.FirmeEntity;
import hr.datastock.entities.IzdatnicaEntity;
import hr.datastock.services.FirmeService;
import hr.datastock.services.IzdatnicaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static hr.datastock.controllers.service.impl.Const.DATE_FORMATTER;
import static hr.datastock.controllers.service.impl.Const.FX_ALIGNMENT_CENTER;

@RequiredArgsConstructor
@Service
public class IzdatnicaControllerServiceImpl implements IzdatnicaControllerService {

    private final IzdatnicaService izdatnicaService;
    private final FirmeService firmeService;
    private final UtilService utilService;

    private ObservableList<IzdatnicaEntity> izdatnicaObservableList;

    @Override
    public void init(IzdatnicaController izdatnicaController) {
        this.izdatnicaObservableList = FXCollections.observableList(this.izdatnicaService.getAll());
        this.setComboBoxFirmeOnCreateIzdatnicaEntity(izdatnicaController);
        this.setComboBoxFirmaOibOnSearchFirmaEntity(izdatnicaController);
        this.setValuesToTableColumns(izdatnicaController);
        this.clearRecords(izdatnicaController);
        izdatnicaController.getTableView().setItems(this.izdatnicaObservableList);
    }

    @Override
    public void searchData(IzdatnicaController izdatnicaController) {
        final FilteredList<IzdatnicaEntity> filteredList = this.izdatnicaObservableList
                .filtered(izdatnica -> this.getComboBoxFirmaOibOnSearch(izdatnicaController) == null || izdatnica.getIzdatnicaFirme()
                        .getOibFirme().equals(this.getComboBoxFirmaOibOnSearch(izdatnicaController)))
                .filtered(izdatnica -> this.getDateFromDatePicker(izdatnicaController) == null ||
                        izdatnica.getDatum().equals(this.getDateFromDatePicker(izdatnicaController)));
        izdatnicaController.getTableView().setItems(FXCollections.observableList(filteredList));

    }

    @Override
    public IzdatnicaEntity saveIzdatnica(IzdatnicaController izdatnicaController) {
        if (this.getTextFieldAndDateDataForDialogCheck(izdatnicaController).isEmpty()) {
            final IzdatnicaEntity izdatnica = this.save(izdatnicaController);
            this.init(izdatnicaController);
            return this.izdatnicaService.createIzdatnica(izdatnica);
        }
        this.utilService.getWarningAlert(this.getTextFieldAndDateDataForDialogCheck(izdatnicaController));
        return null;
    }

    @Override
    public void deleteIzdatnica(IzdatnicaController izdatnicaController) {
        if (this.getTableViewIzdatnica(izdatnicaController) != null && this.utilService.isEntityRemoved()) {
            this.izdatnicaService.deleteIzdatnica(this.getTableViewIzdatnica(izdatnicaController).getIdIzdatnice());
            this.init(izdatnicaController);
        } else if (this.getTableViewIzdatnica(izdatnicaController) == null) {
            this.utilService.isDataPickedFromTableViewAlert();
        } else {
            this.utilService.isEntityUnableToRemove();
        }
    }

    @Override
    public void clearRecords(IzdatnicaController izdatnicaController) {
        izdatnicaController.getDatePickerDatum().setValue(null);
        izdatnicaController.getDatePickerDatum().getEditor().clear();
        izdatnicaController.getComboBoxCreate().getSelectionModel().clearSelection();
        izdatnicaController.getComboBoxSearch().getSelectionModel().clearSelection();
        izdatnicaController.getTableView().getSelectionModel().clearSelection();
    }

    private IzdatnicaEntity save(IzdatnicaController izdatnicaController) {
        return IzdatnicaEntity.builder()
                .idIzdatnice(this.nextId())
                .datum(izdatnicaController.getDatePickerDatum().getValue())
                .izdatnicaFirme(this.getComboBoxFirmeOnCreate(izdatnicaController))
                .build();
    }

    private void setValuesToTableColumns(IzdatnicaController izdatnicaController) {
        this.setProperty(izdatnicaController);
        this.setStyle(izdatnicaController);
        this.setCellValueProperties(izdatnicaController);
    }

    private void setProperty(IzdatnicaController izdatnicaController) {
        izdatnicaController.getTableColumnId().setCellValueFactory(new PropertyValueFactory<>("idIzdatnice"));
        izdatnicaController.getTableColumnDatum().setCellValueFactory(new PropertyValueFactory<>("datum"));
        izdatnicaController.getTableColumnFirmeEntity().setCellValueFactory(new PropertyValueFactory<>("izdatnicaFirme"));
    }

    private void setStyle(IzdatnicaController izdatnicaController) {
        izdatnicaController.getTableColumnId().setStyle(FX_ALIGNMENT_CENTER);
        izdatnicaController.getTableColumnDatum().setStyle(FX_ALIGNMENT_CENTER);
        izdatnicaController.getTableColumnFirmeEntity().setStyle(FX_ALIGNMENT_CENTER);
    }

    private void setCellValueProperties(IzdatnicaController izdatnicaController) {
        izdatnicaController.getTableColumnFirmeEntity().setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(final FirmeEntity item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        izdatnicaController.getTableColumnDatum().setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(final LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(DATE_FORMATTER.format(item));
                }
            }
        });

    }

    private Long nextId() {
        return !this.izdatnicaObservableList.isEmpty() ?
                this.izdatnicaObservableList.stream().mapToLong(IzdatnicaEntity::getIdIzdatnice).max().getAsLong() + 1 : 1;
    }

    private String getTextFieldAndDateDataForDialogCheck(IzdatnicaController izdatnicaController) {
        final List<String> checkList = new ArrayList<>();
        if (getComboBoxFirmeOnCreate(izdatnicaController) == null) checkList.add("Company identity number!");
        if (getDateFromDatePicker(izdatnicaController) == null) checkList.add("Date!");
        return String.join("\n", checkList);
    }

    private void setComboBoxFirmeOnCreateIzdatnicaEntity(IzdatnicaController izdatnicaController) {
        final Set<FirmeEntity> oibFirmeFilterList = this.izdatnicaObservableList.stream()
                .map(IzdatnicaEntity::getIzdatnicaFirme).collect(Collectors.toSet());
        izdatnicaController.getComboBoxSearch().setItems(FXCollections.observableList(new ArrayList<>(oibFirmeFilterList)));
        izdatnicaController.getComboBoxSearch().getSelectionModel().selectFirst();

    }

    private void setComboBoxFirmaOibOnSearchFirmaEntity(IzdatnicaController izdatnicaController) {
        izdatnicaController.getComboBoxCreate().setItems(FXCollections.observableList(this.firmeService.getAll()));
        izdatnicaController.getComboBoxCreate().getSelectionModel().selectFirst();
    }

    private LocalDate getDateFromDatePicker(IzdatnicaController izdatnicaController) {
        return izdatnicaController.getDatePickerDatum().getValue() == null ? null :
                izdatnicaController.getDatePickerDatum().getValue();
    }

    private FirmeEntity getComboBoxFirmeOnCreate(IzdatnicaController izdatnicaController) {
        return izdatnicaController.getComboBoxCreate().getSelectionModel().getSelectedItem() == null ? null :
                izdatnicaController.getComboBoxCreate().getSelectionModel().getSelectedItem();
    }

    private String getComboBoxFirmaOibOnSearch(IzdatnicaController izdatnicaController) {
        return izdatnicaController.getComboBoxSearch().getSelectionModel().getSelectedItem() == null ? null :
                izdatnicaController.getComboBoxSearch().getSelectionModel().getSelectedItem().getOibFirme();
    }

    private IzdatnicaEntity getTableViewIzdatnica(IzdatnicaController izdatnicaController) {
        return izdatnicaController.getTableColumnId().getTableView().getSelectionModel().getSelectedItem();
    }
}
