package com.icl.fmfmc_backend.controller;

import com.icl.fmfmc_backend.service.RoutingService;

import com.icl.fmfmc_backend.dto.Api.RouteRequest;
import com.icl.fmfmc_backend.dto.Api.RouteResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
//import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/route")
@RequiredArgsConstructor
//@Validated
public class RouteController {

  /**
   * This method is called when a GET request is made URL: localhost:8080/route/find-route Purpose:
   * Fetches all the chargers in the FoodEstablishments
   *
   * @return Route
   */

  private final RoutingService routingService;

  private static final Logger logger = LoggerFactory.getLogger(RouteController.class);

  @PostMapping("/find-route")
  public ResponseEntity<?> findRoute(@Valid @RequestBody RouteRequest routeRequest) {
    logger.info("Received route request: {}", routeRequest);

//    if (bindingResult.hasErrors()) {
//      logger.info("Route is NOT valid");
//      return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
//    }

//    if (bindingResult.hasErrors()) {
//      List<RouteRequestValidationErrorResponse> errors = bindingResult.getFieldErrors().stream()
//              .map(fieldError -> new RouteRequestValidationErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()))
//              .collect(Collectors.toList());
//      return ResponseEntity.badRequest().body(errors);
//    }

    logger.info("Route is valid");
    RouteResult dummyRouteResult = routingService.getRoute(routeRequest);
    logger.info("Returning route result");
    return ResponseEntity.ok(dummyRouteResult);

//
//    Double[] start = {routeRequest.getStartLong(), routeRequest.getStartLat()};
//    Double[] end = {routeRequest.getEndLong(), routeRequest.getEndLat()};
//
//    OSRDirectionsServiceGeoJSONRequest osrDirectionsServiceGeoJSONRequest =
//        new OSRDirectionsServiceGeoJSONRequest(List.of(start, end));
//
//    System.out.println(osrDirectionsServiceGeoJSONRequest);
//
////    return  osrDirectionsServiceGeoJSONRequest;
//
//    OSRDirectionsServiceGeoJSONResponse osrDirectionsServiceGeoJSONResponse = osrService.getDirectionsGeoJSON(osrDirectionsServiceGeoJSONRequest);
//
//    List<List<Double>> coordinates = osrDirectionsServiceGeoJSONResponse.getFeatures().get(0).getGeometry().getCoordinates();
//    String polyline = PolylineUtility.encodePolyline(coordinates);
//    System.out.println("Encoded Polyline: " + polyline);
//
//    List<Charger> chargers = chargerService.getAllChargers();
//    List<FoodEstablishment> foodEstablishments =
//        foodEstablishmentService.getAllFoodEstablishments().stream()
//            .limit(2)
//            .collect(Collectors.toList());
//    String dummyPolyline =
//        "arkwHtoiSJg@FAd@Nl@B^CxAg@fCMXMNAVJXd@b@`ALBLCRSTb@|EzGZt@Ll@lBvLPr@X|@xBfEn@r@r@b@n@JZCr@]xA_ATIj@E^FrAb@tBt@|At@dDnCtCzCnBbBnIfGnAr@nBv@TPZ\\b@r@rAdCp@z@ZPvBx@x@^vCfBjAb@\\D`C@l@Bn@Hv@R~@\\`CfA~ErAp@`@h@h@lCnDrA`C`ApA`@Zh@Xv@Ph@BDUJMLALJDTbIgBzBOlA?j@HlAt@xB|A~BvAl@d@z@bAbEzFnBxClDpCt@^zJ`DtAX^LtAz@nA^^y@|CsI~@qDd@eAHQX]tBmBT[Na@vBwK`EgOZo@LOXO`@KxASnD_Av@_@pA{@xCqCbAmA`AoA|DsFdD_EbBmBn@kApAoAlDoF~@_AfBuAVY^q@z@wB~@qDx@yBt@{B`AuB`EuHPc@Jc@V_Db@{Ah@}BZcBR{ARiANg@x@yARo@P[HGHAb@?zAz@\\F`AU^Uv@O~@o@r@{@NW@OEmAB_B?_@Q{@A]BKbGgG|G{Ev@]bBa@nAC|AFxEa@`@AfAFh@JhAf@nDtC|@`@r@Ld@CrEy@f@OvAm@dB}@bCyBtBmDz@yBp@mCzAiCz@iA~@o@~Bs@|EoEvFqR`By@~AcAdB}@hBq@|Cs@zDwDdCiApB_CNONALBnBvB`@h@HBLCR[d@oAt@kDd@eBvCwGl@kATWVQd@MxDi@nBu@pAaAxFeDRE`CA~AS`DNhAVnAAzF{AlAa@|BqAz@k@pC_ChAa@rCy@jAg@d@YfBkBh@[~Ag@VOj@oA^i@PQdE{BpAc@vAk@dC}A|EoChBwAf@k@^e@bAoBb@i@bA}@vHmFp@k@n@m@jBaChAeBpC{Dp@u@b@[h@WbCm@zD{AV?pANpEMhEStGiApCw@zAUzCF|AIzDDjBO|@OZQNONWb@kA~AkFbAsB|@gAr@k@TIzBaAxAa@z@a@n@m@~@iANClCnAxBrAp@j@vBlCp@l@r@b@t@Pb@@xBQbC_@fCMpDVz@@\\AjAUBuAFaAPyA|@{DJo@DeAJgH?iDAsA?q@Fi@^aCf@wBXaAFc@NeDVo@rCaFJ_@l@kOt@cILuBPcBXsA~@gGD_DEw@CMv@cArAuC`@[~D{BzAH~Af@bAHfAT|EhCr@d@`@Jl@APOjB{D|A{DhA{Cp@oAb@s@`Al@pErAz@F`F?~AC|AQ`@AzBn@X?JGTq@lBeE~@gBV]nCyCfDqETc@p@oBnBiH`@gAXi@Z]fCyB`@q@Z[`@QfF}@n@BlB\\Z?TCRIXWt@gBv@_CpBeHbCqHZw@bAoEz@aDr@}BvAuDpAwC|@cCpAsEr@}Cl@b@nArA|@J|@AVHb@T^JdABvAMtFBfC[V?rAC@NPd@^b@H@PG^LHHvDzBpDpCdB~ALP@J^HjCxB^r@Rl@Jn@LjDCt@s@`JMdAGlA@XFZZz@V`@|@dAxD~B`D|A|AjAfG|Hz@lA|@fAzGrH\\RpANx@BpBGfABTMJShAcDLQTM\\I|@It@Pt@`@`B`BjBnAvBt@~ANvCD`EGtIe@zBCzAZbBf@`Bn@bBb@VBVEpEaBfCeAXCJDP^r@|Bd@vBLZJH\\@NG`Ay@RKhBg@zG}APKHKdCxAj@h@^h@Zn@^hATz@h@pDp@rAXt@pAbFXj@zFpHXf@jAdCXb@lBdBbE`BnE`BrC~AjCjA\\\\fBrBXN^@`@MlAq@b@Md@EZ?`@DhAp@lAd@x@f@~ChCpA`@p@HbA?bBOj@Cd@FvAd@t@Hv@MZQZ]z@oBRQx@a@^MdAQzCU\\Bt@PpBdA~B~BfApA\\XnBdA`CbAn@b@lAXr@KXOv@o@jAy@vAs@fA_@j@_@~@c@n@Uv@Gt@?dCf@dBd@~Bt@jARrCt@r@^zBtA~AjAvDzBrFtBfAf@t@d@zAvA|A`Bj@~@hClCxB`CzBxBtB~BtChFH?FGj@rBf@xAfApCb@v@pDzFv@~Ar@fBrA~DlCdJl@`BtAnCzDtGd@z@f@tAXnAb@|Bz@tH^zBNr@h@dB|@pBf@v@bAnAr@j@`Al@bA\\j@LbAFhAC`BUr@A`NuEnAYzAQhCCv@DdC^\\HzAh@rVfJ|MpEpHxC`EfAdEtAh@T|@l@xAnAh@f@xAnBvDrFhHxKdCnD|BpDfCpDpA~B~@vBlAjE`DdNjEjQvApEdBxEpApCxApCdAdBzBzCfFvFnGrEfBbArEdBx@^r@`@~@z@pA~AfAtBRh@pElMz@fD^lBvBdUj@dENhBj@pKL`AVpAXv@b@`Ah@z@rA`Bv@~AXt@j@xBNrATvCH|GPjAFREVODIRK\\\\jI^jH\\zKBfESpQFzCHjAL`AxAdGRj@`@r@bD`F~G`JfA~A`GfGvJ|IhJrHzG|EjJbG|FlDnj@|[jK`GbHvDjHnDjF|BzIbDlNjEnG|AfGlAtGbAfR`Chl@|GfR~Ade@~CrO|@~g@fDhD`@pGtAvE~AnD`B|CdBdEvClCvB`BjAzBjArCjAtA^lATdAN`CRbLNhCVzB`@tCx@|BbAtBrA|BdBjBfB~EhGnLtOlBxBzBxBtBbBxBzAxNzHzBtAxB|AzBlBlBjBvd@ng@vCdDbCbDjBnCpDvGdBjD`B`EfB|EbEjMbDzIxCnHnDvH|D~HzGhLfZnf@bQlYzKnQ|DjFnEfFzBpBrB`BrBvAtBnAtBfAbCbA|FjBpBb@lN~AvC`@pDv@vDjAvD|AfB~@lC~AfBnAxE~Dht@xp@zC~BbDrB|C`BzCnAhBl@bEdAjBZ`CZtBPpDH|BAhCIlEa@nEy@hf@sLrGsAvGeAnFk@pF[fFM~ECnFLtEXxCXdGt@rKxBlF~AzJtDhO~GvD~Ax@VxJlC`Ch@fEp@dEf@jFd@rDNpEJtI?vSK|CG~\\MzLOfFPrCRbCZjDj@vCr@tDpAvDhBbc@zVlHhD|GbCpIfC|KlElD`B|DtBzEzCzEhDtFrErFhFzFjGjElF|@rAhEfHzArCrBfEbB~DjBzE|C`Kd@~AbAfEfAxFb@vCZvCVrDNzCLnEBlEAnBKtDWlGk@xJU~EGxD?vBDpFLdDLxBTzC\\xC`@pCj@dDp@vCn@dC~@rCn@dB~@tBdBlD|FlIrA`B`Ar@r@Xx@Pb@@xBE~@JZLZTRXP^XfADf@Bf@Af@MfAI`@]v@q@x@wKdIcCrBkCtCmAzAaB|BmApBoA`CiA`C{@tBw@xBm@hBiA~D}DbPqAvFwAxFyC~LoAvEgAfDyArDwAxCoBbDeAzAcCnCcCvBo@d@eDpBm^xQeDhBuCnB_CnB}A|AwBnCaCtDsB|DyAhDuBnGeA|Da@jBi@tCe@rCg@dEa@jEcD|_@i@|Ek@fDo@xCo@bCy@jCy@xBmAfCo@lAqAnBmAbBqBtBgAbAsChBeGlCgB~@sClBsBjBwAbB{BfDwDzGeCxDqHvIyB`Ds@tAgAlCs@zBy@pDY~Ae@`EMvBIlBC~BBpEJ`CPtB\\rDn@lF`AnKThENjEFbEShMQ~Du@`HeAtHcAhFqAbFqEvOmAdF{A|HkAfIg@pE[jDi@`IQjEM`FGjF@zEN|Jz@tXFlDBhDA|EGzFSlFUpEUrDa@rEi@rEc@fDyEvZo@tEm@xEYxCa@vFYnFQtFI`G?jGHrFNhFvArYRhFHpDBvMEvCKpFUtF]lGc@fFkAlK}@`Gs@tDwBhJcD~MeAxE_AbFaAnGo@xE]fDk@tG[xFMfCSbIG`JFvFHdEP`F\\`HdAlOjAfOr@xI~AvPhBpQfD|Y^hD`@rEVjDXbENbDXjILnFDtE?xHIdNQ`SWxPYxJYhF_@dEYbCQrAm@rDw@zDq@tCcAlDyCtJsAzEcB`Iq@pEmAzIi@hFS~Cm@`L_@lMI|M?jGFnGNlGr@lMXzD~@vHlApHtC`NjEbQxB`KvBfK`GdZ`CrMbEtVdExWbAtFlAzFfBpHvBlH~HzWdBfGxBpJv@pD~@tF|@tFn@vEt@zGjAtMZxEb@zIZ|LDjCDjJI~LI|Eo@fVe@jNsA`\\aFzs@S~CStFO|HSxRY|KQfEWzDU|C_@dEu@jEiAbEoAbDy@dBuAdCwYlb@_B|BsD`GcBlD}@|BiAtD_ApD}@rEk@lDe@rDeCjUaA`Kg@zHYtFKbEW|NMvCu@xKwAlMgGhi@_ClRU`Be@xBgA|DkAbDaAvBeAnBoErHsAlC_B~Du@vBi@jBq@lCwAtHm@`Fa@fFSbFsApv@SrF]zEs@pG{BhPg@tEe@bFs@hLUnHSxL?`LBzBNvF`@dKVrE`@|EnArLfHlm@j@~G\\vGJ`J@~CGdDYxG_@~Ee@fEeCzRm@bH]vEYbKIlG?|FLrHPdFRtDb@`Gd@bFx@hG~@vFrAbGn@xB`AtCbBlElCzEdD|ElDzE|CpFhAlCh@rA`@pArAjFx@fFb@rDp@tGl@pGjArKpCnVfAtHpAtIxAxHd@vBt@jCv@fCdBnE`BfD~EbJnBhEf@tAX|@`AxDp@nDb@lD`CnXr@`Hf@jETfBjAfItB|Mx@xEpBzJpAxFzC|LpAvFzDnQnBxJl@`DtAdJxA`KnAtKh@bEhApHhAxFb@jBt@nCnBzF|@|BjAnCrAnCbCfEpb@vo@`AbBt@xAp@~Al@~ApAhEj@xBb@tBl@lD^bCXhCTbCNhCr@rON|Bf@vFVjBhC~Ol@nFf@dHRdHC|GIdEWzFSzBaBdQWjEUjHEtC?rDPtFRbEV`DpAhLbP~sAvAhJnBlKxHr]~DbSdBfJzE|YhDrUpBfOvCjYvAvObC`ZnCdg@jBnf@ZjGp@nJbBdOp@pElAbH|ExTb@lCd@hD^bD\\lER|DJdDFjDFdJVhNX|Ib@fJn@xJv@~Jx@|I`AxIdAjHx@jFrAdH~AdI`BrHlDnNbDdKxBjHxAlFxAdGpAfG`FxXhDnQlC~Ox@jGj@jGVlDRnD\\dJXzWLbDR|CPvBj@`Fj@pDhA|Fx@fDbAhDfAzCzE|LfI|RrCnHlCfItCbKv@zC|BdK~@xE`CzNfBnMj@bF^fEVlEJlCJbG?rFEdCm@dQInGBjFF|BLzCX~Dr@vGl@tDX|A`@lBfA|Dj@jBr@pBt@fB|HvPzDxIzGbP|C|HrAbDtAtCrBbElC~EpCtE~CxEpCzDxHdK`JbLbF~FxEhFhEtEpGtGbAhAbCnD~@`B~@hB|@nB|AjEjA|Dr@xCdB`J~ChO|BlJvBrIpCzJz@bC|AhExLtX|AzD~DzKxBvGnBpGtDfN~@~DxClN`DfR~AbLbChTtBfVbApIr@xEd@lCrAjGd@tBfCjJv@dDh@hCj@vDJx@d@nHDhDCrE]xWA~FDbR?bJQjLe@xOOlIwA`nAEhSMtT@bDJzEZ`H\\jG`@pEf@dFd@|Dr@|EvClQ~BhMjBvJbEvRjGvWpC`LxB~HbArC`CfFtJ`QdDrGnE`KvCpHt@vBbCrHd@tB^rBVlBJbALrBLtCL~GLpD\\~Fb@fDb@xBb@dBj@`Bj@tAt@xAn@dAvAdBtBtBtExDfFdE|IpHxB`C~ClEzGnKlBjC`KdMbAtA|@vAv@zAx@`BnAxCdA|C~@dDrWpgAtCdMlAlHn@zFTpCRhDHrCBxHSva@@nGPvU?vMG`EKbEMxDUbEa@tFyApOo@zISbEOdEMdHE|JDxEF~E^vLn@lNv@rM`@hFd@hEv@tF~@bFnAxF`BxF`CxG`B`ErArCzArCr@jAtB~CxAjBhBlB~ArA|InGnEvDrBnB`HlHdIlKhs@xaApB`DnC`F~B~EfBfEjDjJ~BfFvAjClI`M~BbEzCbGpCxGhRtf@nCrFzBjD`BnBhBnBrAlAfE`DfBjAhBbAvY`NnBjAlCvBlMbLdE~DjDxDtMpPfD`EfNxOrCpDdB~BjC|DfBvClBnDzBvEv@nB|@hCbA~Cr@rCpAvFnBbKn@xDZlCRvBHlBD`C?bBKhDU|CwArLQpBGhAG~BA~A@bBDfBLhBT|BZbCrDjVl@nFT|EHbDh@|]HnDLnCLtBj@jGZ~Bb@dC^lBjBhHn@jB`AdCrGdNnBxDzBvDhCbChCxApA^xAXbLrAlCf@|BTjC`@|Af@lAn@hAt@r@j@`A`AdB|BdBhDrAxDvA~FlGvYz@`Dp@vBj@xAn@rAtAfCn@x@hBpBfBvA`Aj@rCpAbExAdBbA`BpAnCtC~BnDpAfCzEbKtGlObG|MzAfE`AdD~@zEhDlV~AbJxBxI|@vCpJ`WzNp^`EjJxEnJjCpEjBxC|A|BnCxDhFpG~JvLfBzBfDrD~DzDtAjA~AjAzDbCnj@nXrCnAjFfBpG~AxHbAhITzHUtH_AvFoA|I}CzCwAvEqCjGkE|CcBdEoB|FoBdCo@jGgAfCWfFQpFCx@BvEXnC\\nEt@rDbA`Cz@dDpA|C|AxCfBlDbCjDtCrEpElDdEdBzBfDdFpBnDvBjElAlCdAhCjAzCdCnHfChJlBxIjAnG~@jGf@hEn@lGd@bGRbDPvDLdEJjEvBfsBJfIZlMRpGv@vPdAbP|AjQt@dH`B~M~@|GfIrg@xAdIvFb]`Fb\\nAtIb@lENfCJ|CHpE@xBGbM@hEJhFT~ERnCZhDn@zEx@jEfElRl@xC|@xFd@~ETvCRhFD|BB`EGtN@nDFzDJbDRjEXhEVdDh@vFd@bEd@jDn@`EjAnGfBlIdHlXhAvEh@tC^~B\\hC`BzOj@pEb@rCn@hDlAxF|@nDp@|BzAtEf@pAjAfCjA|BrFtJ|@pBpAjDpAvEd@~Bd@`DZtCRrCHvBDdC@rCEbDY|NCzDEhMFtLF~EP`H^lKh@pK|@zM`@~H^rJTjJNnM@lIA`EI`EQtD_@nF}@|LKlBMlEAjEFnEH`CNhCVxCf@bEb@pCj@pCn@lCjAfEbCpHpItVvGxQbBzDvAtClBzCxAlB~AdBnAjAtB~A~GtE`BrAxBnBzCjD|BnDv@zAv@hBxAxDbD|JrBfF~b@p~@jAlBnAhCv@zAlB`D~CxE`@\\L?XJPNLPJVFXFt@A^E^IZKXQRQLSF]?e@ZILu@zB_AdDc]frAYnAUvAOhAA`@BfALVDX?ZETNdBb@~B\\hCTpDFxD@nDO~EI`BUtCi@hDS|AQxDIbAQt@uAtDaA~CyAlLUdAYbAuA|Gk@fDEnD\\~A~@`D~AfH~AfJr@jHx@pF~ApH\\vAj@fBRv@L~@FnADzBCbBg@lFg@|Ce@~A]|@gBdD]`AyBzJq@rD_AfHO`BKfBEhCBrG]lIExFBzIDpJ|@hP@fEPnGY~DWxCm@`GSnFAzEHpEFnBVjEbAvMrAj[j@nOFhCL~JHbJAhMNhHBtCYnJ_BbL{@~Dc@lCw@hIK|AGbB?bABpADBHJ@RETKFKAGKi@EeDs@iIaCQAg@FKDSTWb@yAlCeBdCgDnFkCbF_A`CwBzHgAxCi@fA_G~JuApByA~AgDpC_@`@FVARGNKFKAGEw@tA{BnEUZiAjAqAzA{AzByA~AQZQh@OdB?n@InAm@xDM`AOrAq@|Im@tMYtEsAhMW`NIhAKz@FfBIx@Ex@At@BjB^~G^tID~B?~BGbBk@zGa@hCw@fCwA`EGj@@f@RjBb@bCv@xD`@|AP\\HTjAlBvAlB";
//    RouteResult dummyRouteResult =
//        new RouteResult(
//                polyline,
//            100.0,
//            3600.0,
//            chargers,
//            foodEstablishments,
//            //            List.of(new Charger(), new Charger()),
//            //            List.of(new FoodEstablishment(), new FoodEstablishment()),
//            routeRequest);

//    return dummyRouteResult;
//    return  osrDirectionsServiceGeoJSONRequest;
  }
}
