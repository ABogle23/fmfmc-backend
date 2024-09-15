package com.icl.fmfmc_backend.service;

import com.icl.fmfmc_backend.config.integration.EvScraperServiceProperties;
import com.icl.fmfmc_backend.entity.ElectricVehicle;
import com.icl.fmfmc_backend.entity.enums.ConnectionType;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EvScraperService {

  private final EvScraperServiceProperties evScraperServiceProperties;
  private final ElectricVehicleService electricVehicleService;
  private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+"); // extract numbers

  /** Scrapes electric vehicle data from a predefined website and saves it to the database. */
  public void scrapeEvData() {
    try {
      String baseUrl = evScraperServiceProperties.getBaseUrl();
      String mainUrl = baseUrl + "/uk";
      Document mainDoc = Jsoup.connect(mainUrl).get();
      Elements listItems = mainDoc.select(".list-item");
      Random random = new Random();

      //      int count = 0;

      for (Element item : listItems) {
        //        count++;
        //        if (count == 2) {
        //          break;
        //        }

        int delayInSeconds = 10 + random.nextInt(30); // Generates a number between 1 and 5
        System.out.println("Delaying for " + delayInSeconds + " seconds...");
        Thread.sleep(delayInSeconds * 1000);

        // details on main html source page
        String brand = item.select(".title span[class]").first().text(); // extract brand name
        String model = item.select(".title .model").text();
        Double batteryCapacity = extractNumeric(item.select(".battery").text());
        Double pricePerMile = extractNumeric(item.select(".price-range").text());
        //        String availableOrderDate = item.select(".not-current").text();
        Double topSpeed = extractNumeric(item.select(".topspeed").text());
        Double range = extractNumeric(item.select(".erange_real").text());
        Double efficiency = extractNumeric(item.select(".efficiency").text());
        Double rapidChargeSpeed = extractNumeric(item.select(".fastcharge_speed_print").text());
        //        String price = item.select(".price_buy").text();

        // individual ev page details
        String evDetailsUrl = baseUrl + item.select("a.title").attr("href");
        Document evDetailsDoc = Jsoup.connect(evDetailsUrl).get();
        List<String> chargePorts = extractChargePorts(evDetailsDoc);

        List<ConnectionType> connectionTypes = new ArrayList<>();

        for (String portDescription : chargePorts) {
          ConnectionType portType = getChargePortType(portDescription);
          if (portType != null) {
            connectionTypes.add(portType);
          }
        }

        if (!areFieldsValid(
            brand,
            model,
            batteryCapacity,
            pricePerMile,
            topSpeed,
            range,
            efficiency,
            rapidChargeSpeed,
            connectionTypes)) {
          continue;
        }

        System.out.println("Brand: " + brand);
        System.out.println("Model: " + model);
        System.out.println("Battery Capacity: " + batteryCapacity + " kWh");
        System.out.println("Price per Mile of Range: £" + pricePerMile + "/mile of range");
        //        System.out.println("Available to Order: " + availableOrderDate);
        System.out.println("Top Speed: " + topSpeed + " mph");
        System.out.println("Range: " + range);
        System.out.println("Efficiency: " + efficiency + " Wh/mi");
        System.out.println("Rapid Charge Speed: " + rapidChargeSpeed + " mph");
        //        System.out.println("Price: " + price);
        System.out.println("Charge Ports: " + connectionTypes);
        System.out.println("--------------------------------------");

        ElectricVehicle electricVehicle = new ElectricVehicle();
        electricVehicle.setBrand(brand);
        electricVehicle.setModel(model);
        electricVehicle.setBatteryCapacity(batteryCapacity);
        electricVehicle.setPricePerMile(pricePerMile);
        electricVehicle.setTopSpeed(topSpeed);
        electricVehicle.setEvRange(range);
        electricVehicle.setEfficiency(efficiency);
        electricVehicle.setRapidChargeSpeed(rapidChargeSpeed);
        electricVehicle.setChargePortTypes(connectionTypes);

        electricVehicleService.saveElectricVehicle(electricVehicle);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("Failed to complete");
    }
  }

  private Double extractNumeric(String data) {
    Matcher matcher = NUMERIC_PATTERN.matcher(data);
    if (matcher.find()) {
      return Double.parseDouble(matcher.group());
    }
    return null;
  }

  private List<String> extractChargePorts(Document doc) {
    List<String> chargePorts = new ArrayList<>();
    Elements tables = doc.select("table"); // get all tables

    for (Element table : tables) {
      Elements rows = table.select("tr"); // get all rows in table
      for (Element row : rows) {
        Elements cells = row.select("td");
        if (!cells.isEmpty()
            && cells.first().text().equalsIgnoreCase("Charge Port")
            && cells.size() > 1) {
          String portType = cells.get(1).text();
          chargePorts.add(portType);
        }
      }
    }

    return chargePorts;
  }

  private boolean areFieldsValid(
      String brand,
      String model,
      Double batteryCapacity,
      Double pricePerMile,
      Double topSpeed,
      Double range,
      Double efficiency,
      Double rapidChargeSpeed,
      List<ConnectionType> connectionTypes) {
    return brand != null
        && model != null
        && batteryCapacity != null
        && pricePerMile != null
        && topSpeed != null
        && range != null
        && efficiency != null
        && rapidChargeSpeed != null
        && connectionTypes != null
        && !connectionTypes.isEmpty();
  }

  private ConnectionType getChargePortType(String portDescription) {
    String cleanedPortDescription = "";
    if (portDescription.equals("Type 2")) {
      cleanedPortDescription = "type2";
    } else if (portDescription.equals("CSS (Option)")) {
      cleanedPortDescription = "css";
    } else {
      cleanedPortDescription = portDescription;
    }

    for (ConnectionType type : ConnectionType.values()) {
      if (type.getApiName().equalsIgnoreCase(cleanedPortDescription)) {
        return type;
      }
    }
    return null;
  }
}
