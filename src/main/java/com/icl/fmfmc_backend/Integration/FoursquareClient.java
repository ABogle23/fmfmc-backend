package com.icl.fmfmc_backend.Integration;

import com.icl.fmfmc_backend.config.FoursquareProperties;
import com.icl.fmfmc_backend.dto.FoursquareResponseDTO;
import com.icl.fmfmc_backend.entity.*;
import com.icl.fmfmc_backend.service.FoodEstablishmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;

// https://docs.foursquare.com/developer/reference/place-search

@Service
@RequiredArgsConstructor
@Slf4j
public class FoursquareClient {

  private static final Logger logger = LoggerFactory.getLogger(FoursquareClient.class);
  private final FoursquareProperties foursquarePlacesProperties;

  //  private final FoodEstablishmentService foodEstablishmentService;

  //    public FoursquareService(
  //            FoursquareProperties foursquarePlacesProperties, FoodEstablishmentService
  // chargerService) {
  //
  //        this.foursquarePlacesProperties = foursquarePlacesProperties;
  //        this.foodEstablishmentService = chargerService;
  //    }

  public FoursquareResponseDTO getFoodEstablishmentFromFoursquarePlacesApi(
      MultiValueMap<String, String> parameters) {
    int bufferSize = 16 * 1024 * 1024; // 16 MB
    int timeoutSeconds = 180;

    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(
                clientCodecConfigurer ->
                    clientCodecConfigurer.defaultCodecs().maxInMemorySize(bufferSize))
            .build();

    WebClient webClient =
        WebClient.builder()
            .baseUrl(foursquarePlacesProperties.getBaseUrl())
            .defaultHeader("Authorization", foursquarePlacesProperties.getApiKey())
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .exchangeStrategies(strategies)
            .build();

    logger.info("Attempting to fetch foodEstablishments");

//    String polygonParam = "sy`qHxw{^_C{A_CcBmB}Au@q@wAuAuPuQ{DuDoAgAcDeBuCiBeAw@iCwBw@u@eBiB}@gAiBcCsAsBqA{B{@aBiAgCc@iAk@}Ae@yAi@oBi@sB]cBi@sCa@iCSeBQ_BQ}BKiB_@aKMeBaB}Ma@uCE[Qq@uEcOe@yAaNeS{AeCs@uA}@kBEKkAk@gEiCeAu@[YaK{Bc@OuJiFu@m@aGaGiEsHeCsI_AiJQ}FA{@EwCA_A?yAI}CC{AAk@NgGBc@SoAOaAY}BUwB}@uLMaCMeECyB?{BBoBNeGDaALoCDq@v@sL`@kHDw@X}DP_BNqATgBZgBN}@l@wCP_A?Cb@uEwBuDqD{F}@yAiGkL_AmBuEqKm@}AoAmDm@kBgAyDg@yB_@cBSkAYeBSoAOkAKgASsBKuAMsBEiAGcBCaACsBAeA@wR?M?g@cIk@{ASkE{@oA]gEuAgAc@kDaB{@g@sE_DaBuAuC{CUBaIJaACuIq@k@KgGuAOG}A@u@CsGg@YEqBd@aANaGh@{@B_GDkACgJw@]GkG}Ao@UgHcDkAs@_GuE}@{@}BmCi@q@_BcCu@qAiAwB]u@i@c@gD{CkAoAuBkC}BcDW_@u@iA_BoCgBqDS_@_BkDo@{Ae@mAeAaDQq@q@yCMo@c@{C]oDw@gFwA{HO_AUyAUkBOwAOkBKkBGiBI{CAWE[CIeAuCq@{Bg@e@cDiDcAmA{AwBMSmEgDUUQMaFcFQU}EwIkCaKEYi@mG?cCCSCEm@eAsKaQi@_AgBcD}@kBo@wAc@eAyB{FKAUGaJgDsHqFyFmHKSc@aAuLkP}AaCe@{@y@{Ai@gAk@uAk@sAu@wBi@gBgBa@sAe@_@QaIaAWGwI{C_CkAeHwEk@e@wBwB_CkCaDkE[i@eBQ{ASsDo@uBe@aGkBc@S}IgFOMyFeGq@_AmAiBOW{Cy@e@QgIeEUQaCkBk@g@kDsDq@}@sCqEYi@iAcCYw@]_AsA}@wEaFoDeGKUiCeIu@{FiAWsAk@uCs@_@OiEqBi@YqE_Dm@g@wBuB]]}BsCkBmCEIeEkE_@k@yCwFoBkGEOoAqKCi@FeKBW|AaKFWXw@l@wCFSZuAx@uH?A?Ko@kBsAaCc@y@gCeF]s@wA}Cu@kBw@yBw@cCmDwMu@iDWyA[uBCYACqA{Gm@eCe@_Cw@yE]gCQqBWiAsD_IA?Yq@CGEM]gA@?mAcEaAqHAYH}NVwCDc@a@uDGsAD}ICo@qAgDmAgJC@?EU_BiAqA{@iAcBoCuHcN}@iB_DaHKWaCqF{@_CAAwBwBgAoAgDyEqBmD_CcFQe@wAgFGYSoAeCeCa@i@wCkE[m@cBqDYs@q@oB[gAeAmEgAiGc@kD[_DMcBGqAI{BEaD?mCBgCBgAF_BVyD^qDR}Af@eDFc@zAaJX_CXuCh@qHX}ETwEL}CDiC^ik@BsFCeCE_AmDyj@OeBIs@Ks@Mk@I]e@}AgCoHuA]mDcAaA]uBy@??yGaBeGoCKGwHeGuFcIKSyCsIqAkJCa@GgIB_@}@{@eEoE_AcA_BoBi@o@oBkC[a@iKaOe@s@s@iA_A_Be@{@eB{Dg@uAu@cC_@qAk@gC_@kBi@yDSuBMcBScDCs@KiCEwAE_DAwA@qE@[DcG@_@FoDD}ANaDHuAToCDi@dAoKBa@?ACUc@eEEi@WsCKeBKwBG{ACcBA}A?qCDcCRaGBi@f@oKDs@PaCHcAb@qEVwBj@sDXaBbA}ER}Bd@oDZmBZyAhAgFBK@o@AGCY}Eob@Ie@??iMs`@sFaMWm@{@{BcLoScCuFiAgDqA_Fc@aCe@}Ce@gEa@g@u@aAe@o@q@cAc@o@oA_CsAkCcBgE_AwCu@kCo@sCc@uBSqAYoBeAkJAGQ}@Mc@GQQYqDgFoAmBcAgBw@wAoAkC}@qB}Zgx@EK_CkG_@eA}A{EcA{DmAcGSgAsW{~AEWkDiUOy@AC}ByCyBeDq@oAsCyGuAsEo@_C_CeK_BsEuC_IwA_F{CmNsA{Da@wA}@}BeDuJSm@YaA}@oDSaAm@iEE]WkECiAAiCB}DS}BGw@OcCGkAQcEEqD@mB@cADiBBm@L{DHwAFqECyGhAwJHc@Vw@l@}BC?ZsABI@KD]e@gGIyAOqDEcBCaCAe@C}L?S?sICqBk@_Ck@mAcBgGCOe@oDkAy@aEkEIKaDiF{B_GKa@uAgHa@qH?e@?sBDgCCiF[g^I_Ds@yCg@cCw@cFKQyBwEmAyCYw@w@qCo@gCWiAuBkKgDiM_@}Ag@aCMu@Ki@MWs@{Ae@eAmAkDYaA{@uDWuAWaBSsAGk@W}Bq@yBSoBEQ[cB_B{JqByEeC}Eu@aBg@iAcCeIWuAq@uEGo@MaBeAyAOW}C}IYoAq@sDo@yEO}@uAwGYy@aEsBaCuAiAu@kA_Ao@OaIyC}GsEmFiGuDqHsBqIo@_JTcJzAwIH[zC{HtEwGFGvGuFdIsDv@QXOHClKiCrGYhBYXAnHKlHf@rFjAtC\\dIfCbHdEvFxFbEdHnAbEn@^~EpEJJnFpI^|@bAhCp@lBvE`M`AxCJb@b@dB`CjLTlAN~@zB~CTd@jCxHRz@v@pEb@fDHt@N|AL|AP\\t@~AvD~I`AlCp@vB\\fALf@f@zBZ`BNz@`B|JPx@Dd@N`@VbA|@jF`@lDdA|Bj@zAxAxEd@pB\\bBb@dCPv@hDnMb@jBtBfK`@|@PZ`ArBh@pAbArCz@nCx@`Dd@xBTnAt@tE~@|D^dB^rBh@dEL`BPtCTnIBjARpV|@hAT^xCxGt@zBv@lCfBrHVz@XdAj@~Bp@jDl@tBlC`Ln@dDXfBVpBNdBRxCF|AD`BDrF@p@?bJB|K@|@FnAd@tGJlBHrBBpABrB@jAA`AEbCEjAWtDg@vEOjAU~AWxA[`BWnAA?ItGSxE?@GzAAd@DbAF`A^nERhDDfBBpCd@tAf@pAXv@E@v@zB@?zBvGfAzDtC|MhB~EJVjCxHfAtDtBhJ^d@rC~DfAjBjA~Bv@bBp@bBv@vB`A|C^vARx@Rz@X~An@xDF\\lDlUfWh}Ad@`Cf@zAjB`F|Yfv@Td@@@`EvFdBpCbB~Cl@jAVl@l@rAjA|CdA`D`AjDb@lBr@dDX|AVdBRxAx@nHrBfC`DzEfAtBtAzCd@lAn@dBZfAh@jBTbAd@xBX~AXtBl@hFfJtPzA~CzAtDlGtNfArClOte@~@lDh@bCVvA^`CVbBbFtc@^tCTzBHlAP~E?tBCtCO~EAZIlBO~BGt@]tCm@xDc@vBiAdFKnAQ~AWnBc@hCiArFQhASrBGbAc@|IMjD?^?F@RJjAf@xEJlAPbCLnCH|C@dCAlACfBI~BKjBOvBIx@eAxKKnAEfAEnBEbFAtCBfA?J|FjIlA~Al@t@vBxBtD`D~@z@zBxBxE`GbA`BfBfDp@|ApB|Fd@hBj@nCH`@tCbA~@^xBbAnAp@fEnCl@d@nB`Bh@h@dAdAp@t@xAhBbAvAlB|CzAnCbBtD`DrIPf@lDfKVv@bAfD`@zAf@lBThAf@bCZfB^jCNfATrBFt@X~CFx@vD|l@FnAJzCDfBBxE@z@CzG?P_@nl@Av@I|DCl@OhECj@Y~FCb@_@xFATo@zII~@a@dEEb@g@hEWnB{AxIt@n@dBjBxAhBf@p@~FpLJXfB|IJzBr@t@n@z@|CxEd@~@xBnFvAlErA~CzB`FfCrE~@l@`@\\pCjCZ^xEdHl@jAtCxHXdA|@tEHf@Hv@PtANfBHlAlCnFpB~Hr@pI?PCfFRpBB\\CtKC\\ZjEDdA@zEAt@j@vARz@fAxFJx@`AzE\\hBPpAThBRlBTvAb@fBTdAfBbJRjATvAL`ApBnH@Bh@lAfBlDjBfDbAnBx@jBbAhCjBtFd@zAV|@Nl@jAzEv@`EJv@^zDJnBJzD@`F?hA?^xElCrA~@hA|@nB~A~BzBzA`BvCvDnAjBrA`Ch@dAz@lB^~@f@nA`@nA`@nA`@vA`@~A\\xAZ`BX`BXtB^fDR`CJfBFF~@tA`@n@|@fB`Bt@jB~@jFfDpB~ApFId@@`In@tHfB\\JxErBTJfGfE~ErFPVxClFpBbGVdA`Cx@nAHp@NjCTlHjBh@PlGvCPJPJdGlD`FxE^b@|B`DhDvFn@fAlBpD~@pBh@nAt@jBr@vBPf@nAbE`F|GrD|AlCtAxBpAt@d@rB|AdAx@pBjBhAjAnAvAfApApAdBbAzAjApB~@bBn@pAb@`ARd@p@|AXp@nChHp@lAfKrPT^hB~CxAtCtAdD`AdCj@fBf@fB\\pAPr@TdAXrAPdAnC|C`@h@|AbCBB|E~Dz@|@`F`Hp@jAxBdFt@vBVx@xBlH|@vBjB~FT`Al@hDb@~CJp@`@hDT|CNrCF|AFnCtAvHP~@hAvHR`B@@h@fAPV`BpAxBlBTTbEAbADlGh@lAP`B\\rBAL@pAq@hGgBZGbNmAnBA|FJpAH`IdAb@HzFdBhFfC^TpJtId@l@FF@?VLvFdFdCtCf@f@|@Bx@FpD\\z@LvCf@vAZlDQtGLnAHbK|A^HzLlFt@d@fHnGTVtDvFfCpGvAdH@HTpCLt@j@lDVpBRrBL|AJbBFnAFfBDtA@hB?fAE`XAlQ@l@@\\B`@Dd@DV@@ZfA`@hA|CjHlEfIhDlFh@|@pDpGNXfDhGvA|C`@dA`AnCZjAp@nCThA\\pBNhAV`CNdBN|DBtA@fBAtAErBEhAQtC{ArPMhASxAWbBc@hCKh@i@lCCJMdB_@bHCd@w@jLGrAKjD?BDdAj@xHD^Jh@\\|A\\rAzAdJH~EtDlDt@jAjDxAr@`@xE`Dr@l@tDtDd@j@`ChDv@pAdAnBf@fA^x@bC|FzM|RzAfCfAtBz@dBr@bBv@tBjAlDL`@lF~P^tAt@tCb@rBb@~BTrAn@tEHl@nBhPPlBZhEHzAXpHPJlBnAhClBrD~Cx@t@rFjFv@v@lOjPbDjBrGzE`FlGhDtHhBnId@|I]~IcBrIaDvH{ErGoG`FsHhDqIhB}Id@_J]qIaByHcDkFyC";

    FoursquareResponseDTO responseDTO =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .queryParams(parameters)
                        .queryParam("limit", 50)
                        // 50.719334, -3.513779 2.7 Exeter Centre
                        //                    .queryParam("ll", "50.267355,-4.060051")
                        //                    .queryParam("radius", 20000) // metres
                        .queryParam("categories", "13065")
                        .queryParam(
                            "fields",
                            "fsq_id,name,categories,closed_bucket,distance,geocodes,location,price,rating,popularity")
                        //                        .queryParam(
                        //                            "polygon",
                        //
                        // "y`yoHnd}]wa`@wn`G_n}AcvwEwp{Bj{dAmuCn_bGdn~ErzoH~cb@itcAhaAgppA")
//                            .queryParam("polygon", polygonParam)
                        .build())
            .retrieve()
            .bodyToMono(FoursquareResponseDTO.class)
            .block();
    //        .timeout(Duration.ofSeconds(timeoutSeconds));
    //            .retryWhen(
    //                Retry.fixedDelay(
    //                    3,
    //                    Duration.ofSeconds(
    //                        10))) // Retry up to 3 times, waiting 5 seconds between
    // attempts
    //            .doOnSuccess(
    //                    response -> {
    //                      if (response != null && response.getResults() != null) {
    //                        List<FoodEstablishment> processedEstablishments =
    // response.getResults().stream()
    //                                .map(this::processFoodEstablishment)
    //                                .collect(Collectors.toList());
    //                        establishments.addAll(processedEstablishments);
    //                      }
    //                    })
    //            .doOnError(
    //                    error -> logger.error("Error fetching food establishments: {}",
    // error.getMessage()))
    //            .block();

    return responseDTO;
  }
}
//  private FoodEstablishment processFoodEstablishment(
//      FoursquareResponseDTO.FoursquarePlaceDTO place) {
//    FoodEstablishment establishment = convertToFoodEstablishment(place);
//    return establishment;
    //    return foodEstablishmentService.saveFoodEstablishment(establishment);
//  }
//
//
//}
