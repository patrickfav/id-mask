package at.favre.lib.idmask.ref;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.idmask.IdMask;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Tests with reference values. Use this to check for possible regression issues or
 * to tests against other implementations.
 */
public class IdMaskReferenceTests {

    private final List<Ref<Long>> longMaskRefConfig1 = Arrays.asList(
            new Ref<>(-2L, "qqpBIy7v-ib3lufS2gQBLew"),
            new Ref<>(-1L, "zc2O05ybb7CYNgeSqwc8DF8"),
            new Ref<>(0L, "ioq-UAEcDpD-ESocgVtDXLc"),
            new Ref<>(1L, "hobx6l6qsGn1TydWUOaCczs"),
            new Ref<>(2L, "WFj4dnFoIuKLjvAg4Cl-UEM"),
            new Ref<>(-1992581640758283126L, "aWn57km_e11aVIaSpbnzH2I"),
            new Ref<>(8869840532652477687L, "LS35-w6_-uoVD61s37eKj9Q"),
            new Ref<>(3197860861307686743L, "tbXdOST9RricbOEhGNR-aYY"),
            new Ref<>(1164979654160545592L, "1dUZhyDwNBAdIxrYE2USPDM"),
            new Ref<>(-7119569080741756827L, "hITtg_RBxuW5_PTKdt0N44o"),
            new Ref<>(-7144257434397542029L, "d3ckOZv16j2VL8hj5T6i4-0"),
            new Ref<>(8778716370682857110L, "HBzdjtaa_gM2z9hBGFCvLFI"),
            new Ref<>(-6560841423429470858L, "CQlEteALgAtL4vxPLn9pbcM"),
            new Ref<>(-5868949372665991355L, "NDSjU21PfHOmDc97RyxT_aQ"),
            new Ref<>(-6537060410805680606L, "NDSvPVB223jLDaSaYMe0VuE"),
            new Ref<>(-4357931365890710281L, "XFyzWHVadxe7OL32hcakn8g"),
            new Ref<>(939879133301615687L, "5-cp4Dw5CoV2m8bzgBMj26Q"),
            new Ref<>(2003508339046962714L, "Z2cKRNFCVK4ip0SHy4WLtGc")
    );

    private final List<Ref<Long>> longMaskRefConfig2 = Arrays.asList(
            new Ref<>(-2L, "XK5GGVQNI6CF5TDLJYZDQOAUGSCA"),
            new Ref<>(-1L, "NJVJUENXUEB5IXPKZ4QH4A6XNFOA"),
            new Ref<>(0L, "DAME5C3TMGSWJYKMELIPHC6PJDDQ"),
            new Ref<>(1L, "ZDEONWHG7C6P3N6EOQQ2BEWMWCVQ"),
            new Ref<>(2L, "L5PXV4KQ35THCT4RZU3UP6HPDFVA"),
            new Ref<>(-3715919456193561519L, "P572WQJP3KTNBVJR5KU4N3FEF7RA"),
            new Ref<>(-5481516256517375583L, "AYDBTOII2C3DGEOSYMFMGLAOAGSA"),
            new Ref<>(-7468296712281509587L, "TKNH6CYW25GS55M6HOX3XZEMPJUQ"),
            new Ref<>(3021518096685390538L, "VGU55NBMXGJ2LHUAKPNMVCK6RB6Q"),
            new Ref<>(7914997267802934666L, "YHA46PJMAUKPYXLQZ54SKRACTYCA"),
            new Ref<>(7459310047101915058L, "FQWGD4WT62QGKJQVY6JKP67V6V3Q"),
            new Ref<>(-5877777489596283459L, "ZHEQH3HKYLRAXSZRRX4JPSBJ6X6Q"),
            new Ref<>(-4973865568705318815L, "KZLI66Y6BA4IXAJVVMFNWVR7V5AQ"),
            new Ref<>(-6844849186477517873L, "5PVZKJF73PNNPPFE7FX5DWJHEY3A"),
            new Ref<>(-5441115357490558097L, "YLBKIAKMT3KPQAXMLAMOK6VLWEFQ"),
            new Ref<>(-6091878465560117938L, "GY3PJHR7WPDIS7MLWC5OV4IGKLOQ"),
            new Ref<>(2724954116897017160L, "VWWQPQWGAV6YBAOHEKLBGGEWCQMQ"),
            new Ref<>(3826254288628210140L, "CAIFGE3ABDZDU3G5UWWKH5GEPK5Q")
    );

    private final List<Ref<Long>> longMaskRefConfig3 = Arrays.asList(
            new Ref<>(-2L, "a6a62c6991192040300df07b9995b441b7"),
            new Ref<>(-1L, "dada30c0295e33d0afdaa436eda4c1fc85"),
            new Ref<>(0L, "2a2a6dba410f67be98535c3d9b57c36d38"),
            new Ref<>(1L, "8282b35dd5cc9b76979132272cb3cf27d0"),
            new Ref<>(2L, "afafefc71c78647a3b80c1cf523814bb07"),
            new Ref<>(1988377478058312206L, "9797a65d4906c6a90bd9f6eb296624975b"),
            new Ref<>(7100210617136260532L, "6161a8e43422e0140c947bc975f9645b6b"),
            new Ref<>(8205188247681335789L, "abab400a7a405848525dc4d9822ac6e47d"),
            new Ref<>(489639605469815696L, "141412ac72b116e4363bef7852a2195944"),
            new Ref<>(-1547412311625855079L, "82825402124b9f4b5f002d90c47320765d"),
            new Ref<>(8496704857580662741L, "c2c2cf9b7380eb7d78d3e19b526e93703a"),
            new Ref<>(-4875312764385472367L, "1f1f38e7470dcb3339ccc05037684df84d"),
            new Ref<>(-2931344311684914531L, "dddded1849ddd84012b5fe4b2763eab4a8"),
            new Ref<>(-8653657944362799309L, "c8c8d409567213d43d80d91be682541f09"),
            new Ref<>(-3218831467128830374L, "7777bc98cafde1bf419a1caf5d3b7ee3b2"),
            new Ref<>(5565782657656618752L, "1e1e1d7c2d786a2f80ec2abe2cd193786d"),
            new Ref<>(-4234136322513752068L, "fdfdc91cfffba8928c815590e77e5eabe8"),
            new Ref<>(-3854236355167266687L, "7272c70b784621c5b845e58466c941af50")
    );

    private final List<Ref<Long>> longMaskRefConfig4Random = Arrays.asList(
            new Ref<>(-2L, "g4f0I3ATr0skg1lmdRKY_2E5dm5HGaNW3A"),
            new Ref<>(-1L, "cO6DSlwuz85IcOo5FAUad4mjohScWECYpQ"),
            new Ref<>(0L, "Z0c--ezllr54Z5bOLNGqrZ6bY0BkqFJjFg"),
            new Ref<>(1L, "BPZdWi8LfM6RBFuUmu8qVFI6W4hKLI3BZw"),
            new Ref<>(2L, "kr5RZxnPvZq0kgK1VyPsJdI-lmUIeU1eAg")
    );

    private final List<Ref<UUID>> uuidMaskRefConfig1 = Arrays.asList(
            new Ref<>(UUID.fromString("a8267e87-b53a-4e4c-bf53-b0c30187ed76"), "9ve4S_vlijxe1oFD3GKi2uCMJagQRdBUGg"),
            new Ref<>(UUID.fromString("204cb012-f48b-4c1c-b35c-c144b6574fdd"), "lJUvGs0dco5_lshFWFjKi7iFP38IBYmPaA"),
            new Ref<>(UUID.fromString("686bc23c-fa3b-44bc-afbe-9187fc59ecc1"), "c3JGJdDiXUzvmDv9mdMRFqF3BG3wsKtIiw"),
            new Ref<>(UUID.fromString("9079a64b-8db7-4cce-b454-59a1eb6385b2"), "tLWtcAiMSZzyQUl8qTrr0K6zVbIlOEoQxA"),
            new Ref<>(UUID.fromString("02faa72a-c758-4bf4-a91d-8c0fab86eb49"), "Xl97P1NUFLAgJXa8W7kuWevfABUXry2zvg"),
            new Ref<>(UUID.fromString("510ed9cd-1ffd-420f-ae87-25adcae168f3"), "_P1_dOOfQfTooEUhAm8mtvYM5IdyGhKylA"),
            new Ref<>(UUID.fromString("8429f509-b532-44b1-933a-da86fb43fc2f"), "kZBu8tZplxjkclG5GFA2AuTGZSfoaCV9Tg"),
            new Ref<>(UUID.fromString("401b13cc-33fb-4682-bcc4-9b0fa4e2d3c7"), "OTh30HimT7lXha7Hk4q0Lr3cJ_bYYFZpqw"),
            new Ref<>(UUID.fromString("e4909de4-7521-4dea-ae06-b0857029781b"), "MDHclz6V6x1iS4sTDtIE2pmJhlM-mFQZ1Q"),
            new Ref<>(UUID.fromString("e2d095b9-a971-4b0c-a71c-50bbf1a45b2d"), "JCVBZzQgjTWpRSB6RhWU22oNSBvPk5IgRw"),
            new Ref<>(UUID.fromString("afbaf3ad-dfcf-4183-9142-eb5d43d4e38d"), "U1LAV4LT5YNT6ufYgDCcd6YkhdS4mgC22g"),
            new Ref<>(UUID.fromString("8847b309-aace-49f3-af59-2d91717c89d2"), "0NHvKusQCppeX0Ej8FiVXk5h1a7i1SoFMQ"),
            new Ref<>(UUID.fromString("a99ba13f-8fe0-4e0d-9ce5-28f411d35ad5"), "t7Y_-Ew-inU7ap2fqb2EA6fNQJ0_7sBrjw"),
            new Ref<>(UUID.fromString("43e97293-aef0-4e54-a034-d1ba7a3cd50d"), "8fAvbqwLywQhQdGqnMCqS9XSXMZEKv5nsQ"),
            new Ref<>(UUID.fromString("15057e37-54e3-49bc-83ad-b73c561e485b"), "x8YdNxA1-pnJtEaJ-14ugRjOYdH5QeymSQ"),
            new Ref<>(UUID.fromString("ed664da2-70fc-41f2-bc59-189fa2506d7f"), "S0roA60_V42_o-IeUmOOoPNIuvHPwTdlFw")
    );
    private final List<Ref<UUID>> uuidMaskRefConfig2 = Arrays.asList(
            new Ref<>(UUID.fromString("870c655e-c5c3-497d-81a2-06c53a1bca29"), "p6apPCXMhOBVJtjj2szVyH9b1iSkP2-tvw"),
            new Ref<>(UUID.fromString("0592985b-f4f5-44c7-8dba-59be2117faf9"), "ZmeVxUN1ZauwK5tdCf-LYAL5mQB4ncxLaQ"),
            new Ref<>(UUID.fromString("729d0e2f-a4be-476b-a0f5-9e9f1c9f408e"), "uruswHZKEdwcsbkAW3aF5gxlqCS_kG4qaw"),
            new Ref<>(UUID.fromString("1c33f872-ed0c-4914-b408-9f74ad92fc00"), "GhtPuY0qlhn10je07R2Pn553x7AKNr0L0w"),
            new Ref<>(UUID.fromString("f48a3ced-c7b1-459f-8037-d6065b367058"), "WFlTf0uqZA6ZQa2sA95Sxs3xEymPs2HoeA"),
            new Ref<>(UUID.fromString("d6cc10f7-cac0-4ceb-8062-9fae4484943e"), "QkNSgvWM_yVnUPxEPMJBsL4vGmMnKNYzAA"),
            new Ref<>(UUID.fromString("470a4c2d-cbf6-47f8-be7a-cacf6ba490aa"), "FRQ1RBheHWvTcOi3LTSLS_cq7__2Qj61Sw"),
            new Ref<>(UUID.fromString("b92c29f7-7a15-46cf-b0bb-5ba5645c4a2b"), "BgdvKNtZ5cZyIrcMBmu6yb-oe0L3Kf9Lhw"),
            new Ref<>(UUID.fromString("89b879fc-f1bf-42f3-9d07-14242c8892d4"), "19ZzRqX-DKiK1THpCYzmn0re4YBLxiF-iA"),
            new Ref<>(UUID.fromString("1bffa8f6-59af-499b-83c5-6beae7d1a5b8"), "1dRO-B9kcCDKNENsyqQksa5upiiwjfBvvg"),
            new Ref<>(UUID.fromString("5bce7afd-2d8a-45bc-bda0-fa448f817dad"), "BQR-iDOVgGiItLOEciUqgo2JmpSW65rHpQ"),
            new Ref<>(UUID.fromString("23011bcc-47d2-4e6e-9b52-33fbed2fc262"), "BwbuFdhhzQpVV-E4WxUkMEV4culK7RcocA"),
            new Ref<>(UUID.fromString("e87050cc-0f3f-4621-af61-da97aadc9113"), "qqsGklFabrFqhwBsGVNDDGZbRr-Pp8W5ZA"),
            new Ref<>(UUID.fromString("baf83eaa-11a8-4fcc-b10e-812e0702e83f"), "goNL-b7fprs9yVHkJOLydKE2Pi9oeORXOg"),
            new Ref<>(UUID.fromString("07348ece-5298-456c-8232-c44f8b0f0e10"), "FRToD2qmi3zq_qqgIaCCIJ6pdgJ63vb__g"),
            new Ref<>(UUID.fromString("e0796285-ca8f-49d7-866a-7b5bef8a5259"), "7ex6DP1WLnGZzKweCyQtTjdOMiuN3_4izQ")
    );
    private final List<Ref<UUID>> uuidMaskRefConfig3 = Arrays.asList(
            new Ref<>(UUID.fromString("db51451b-a70c-4569-b81f-66aba2dd64ae"), "3dxCjZYyPH7cyrG0V90Um6UmsQ1RdMMP7g"),
            new Ref<>(UUID.fromString("c95dd95c-5242-4175-8581-4073ced9dd77"), "Dg_mx5X4J6kgTIYqHWJyh4NVH9QTqO714Q"),
            new Ref<>(UUID.fromString("11ab86a7-28c1-409a-8e73-0945d5aa1963"), "2dgOauSBGRqAurGhvvQDsFj--jxj32I6zQ"),
            new Ref<>(UUID.fromString("810f796e-0884-4664-941b-3b1e05f82026"), "fXyoUvvuiYHXfmiuXKNPTKKU_fNFGaC4UQ"),
            new Ref<>(UUID.fromString("14e44056-d449-4572-85d0-b9a128aaf114"), "ycgcU_llOFGixDPfSW9WRXjBZ_UkypwKFQ"),
            new Ref<>(UUID.fromString("b4a8402a-d873-47c9-a702-809363cf3568"), "ODnZhvw4Lr4yMflcgW3yIZhY5Pafk5FCqQ"),
            new Ref<>(UUID.fromString("ef3c11e2-0b70-40bc-b634-7250285f9e14"), "jYyz9M0W7qhhb38INW0pvqyKjpwKxXR93Q"),
            new Ref<>(UUID.fromString("94b70f11-3f40-4321-a2a8-438067deea3b"), "RUQnQzdlqrU2P7OKAZXt2kaU7tLhmzRMKA"),
            new Ref<>(UUID.fromString("ce7727f4-7dab-4462-83a5-820304024176"), "Dg-7yBU4Wr5loT35Uz-nVBJrBcLcfFQ-dQ"),
            new Ref<>(UUID.fromString("ab9a6170-55ad-47fe-bd8d-e2dd9e4bbed8"), "wcA8oK5UPCSlMZcLi-XR0qgn3rUesssF5A"),
            new Ref<>(UUID.fromString("8efee31d-41e0-4a55-b194-aec5b8c5e4ea"), "2NlbtQkW5grlsLvSdky6ZWZAHdy3qqjs3w"),
            new Ref<>(UUID.fromString("f4aa5a90-a48f-44f9-baa6-d49258272fc4"), "WFmbQbPt3729QHtt4Ud7KJyqL92LAazT1A"),
            new Ref<>(UUID.fromString("8e8e22c7-c24d-4604-9136-0325e59195c9"), "i4qDhy1yqeoYFvqkaDxBsUC6dubjozekgw"),
            new Ref<>(UUID.fromString("6a6fcc9d-2129-42a7-97d7-030ebdde8426"), "8vN0Jj_xvSoG2yHe99yx3mZ2fj9Gys6LEQ"),
            new Ref<>(UUID.fromString("33a7b4b1-4827-4438-baa6-d9cdcf16f734"), "q6o_7f6L2BrYIURPNDPZ9eC8_HgWY4vaOA"),
            new Ref<>(UUID.fromString("45495c3b-f4e1-4555-a133-c3484c1e1443"), "FRRyv4tX2wCny-LAsMaO8OWsGAvlQHZ_RA")
    );

    private final List<Ref<UUID>> uuidMaskRefConfig4Random = Arrays.asList(
            new Ref<>(UUID.fromString("ea9ab093-3514-4dd4-88fe-f42d177d5a80"), "ZnHwgVT_Ud7rTRB4M_FJarlncT0YIxHTMa17cuF6Msj_qjJi6GHtgd0"),
            new Ref<>(UUID.fromString("6133b08f-f0f4-4f72-ba49-2adfd74aab88"), "4CqBt-MZB3_kLLsR0_ovuIvhbwHKtOIMr97-PxuJQX2Bm5JSLvdk98Y"),
            new Ref<>(UUID.fromString("13f40cb7-bb52-4531-94ea-68250cf18c97"), "5fVtImKgS6850ENTNHpR4oTkGdaLfvvR5k99aQU4XRPCkL4jZWRFJPs"),
            new Ref<>(UUID.fromString("6fb986a4-5787-4193-8753-78a5e24d24c4"), "HiXGNNhYzKA22_83mX3_cksfyxluDGngiKdLQuixy-_FRTvFH9hrpoM")
    );

    private final List<Ref<UUID>> uuidMaskRefConfig5HighSecurity = Arrays.asList(
            new Ref<>(UUID.fromString("0fb38faf-c2f1-4ee9-97ac-11c327a4cc5f"), "-fjMUaTtB9HUeG_NkjjQkQLpe8Zi_029iE1Xzd-WdeTs"),
            new Ref<>(UUID.fromString("7580ce3c-d04a-46ee-98d3-7a83b2e2816f"), "nJ1whAJFhGk5JZTlog4PDQT37DzPWwEZwWnx4UgcGDia"),
            new Ref<>(UUID.fromString("5f7f3d41-f37e-4a89-a184-7c31f43dba72"), "v77lhQAJ94H6GaASSXJU3xh10LqnJHTrAIXxo9yopP9r"),
            new Ref<>(UUID.fromString("c1fbec8b-63be-40d1-b4fb-1bdb5ba73ff9"), "p6YyyBUUb7KCDKr4X1io9BAXtUFLGRCpUy9xOQJGyp0P"),
            new Ref<>(UUID.fromString("338c8646-0332-4a22-9116-0d5b3cc628bf"), "c3JPXtzBQCQiv1zl4OSQ3m2Up8kE5Uptr0GFgzC4Ice5"),
            new Ref<>(UUID.fromString("2fd48591-9a44-472d-b208-358c0617fba9"), "EBEJ726fQfqF1dvtPtGPpsyQeIotMhKdk0l1qgf_eupL"),
            new Ref<>(UUID.fromString("ecc670f0-5651-4c6c-9ace-d69d22972430"), "SUgSQ3lMZUObA9VJAloxstZPuPcgt5WmkP8IP9jLXlBS"),
            new Ref<>(UUID.fromString("015c9484-4772-4c72-9c66-f507f6d3e685"), "AQDJ3V6rAY1aLeyniXpnX1L5rWrYPvpiW-eMm95x5UZJ"),
            new Ref<>(UUID.fromString("860b6910-998d-45eb-a0dd-50eca9d25dca"), "8vNg_w9lO5QwaYylHH1W5M0HatZ59ab2FfBisUpqdKwL"),
            new Ref<>(UUID.fromString("d75fb4af-9d7a-4370-b3d1-56cede2bfeaa"), "i4o8m94T6lx9vSoIHQx3Z6CYt7JzzeL41N9tymI-SLQx"),
            new Ref<>(UUID.fromString("5608cd3e-bdda-4cff-8323-83e68d9c4855"), "yMny0tqK_Exa-7YuO3UlhXaEiLVeRxg1ZSAx4k8gBQ0w"),
            new Ref<>(UUID.fromString("6a2a9177-f534-410c-b867-093bf58f9f7e"), "R0b8LxVKI6ibdhUp4FblbSlUcvxvMQPGqgTea-prN6wV"),
            new Ref<>(UUID.fromString("26695aaa-ff80-419e-9135-8acc0202d8d0"), "DA04svRZCEPzHkUKt6wGFVtke1uF9rZVxFCqWUjTmxtm"),
            new Ref<>(UUID.fromString("dfbbbc7c-0b7d-4dea-8e8c-7e4206f97a28"), "iotFTA8I-DEzu1YiiGUcTw-MZswNC3CIlq8eKcLR-jYt"),
            new Ref<>(UUID.fromString("87bd8ae2-3aea-4644-811e-d5718cbebadf"), "Y2JfOcejD-SBHV_wL7BsWa7BBKZQrrpZLUHUAZQcpOuF"),
            new Ref<>(UUID.fromString("87618fb6-68e8-4194-b7ad-e1f0023c24be"), "29rzXDblGa4XUk1wXApAJyUk2SrZNmDvuCppFtryhR4_")
    );

    private final List<Ref<BigInteger>> bigIntegerMaskRefConfig1 = Arrays.asList(
            new Ref<>(new BigInteger("105553770177495"), "CQgYqqHF6TY73llTvTvaYJ98a-NwpyE-dg"),
            new Ref<>(new BigInteger("1906419158208488013786"), "AQCRCRXGoCY-AYT_DID0ssFKAUS_EiHN4Q"),
            new Ref<>(new BigInteger("172268414386"), "ExJNDXHsoXh5O6dxsn_e5oaTChjmvhn-eQ"),
            new Ref<>(new BigInteger("-1438038716"), "c3JMr160EH9C9vtPQOgephz2SGXf0sDAQw"),
            new Ref<>(new BigInteger("3210714877303297897"), "Ozpzl1-iPLVi1k5cpiymXDOObvDu51g5XA"),
            new Ref<>(new BigInteger("258"), "VVRHTyeLmgxtTo0fZlIxcFfHtGlJo5IyhQ"),
            new Ref<>(new BigInteger("-102156455271390838530260344"), "wcCoRohQUvIfOkWUn6ualYHU6Uwem-0vVg"),
            new Ref<>(new BigInteger("-1146857189"), "MDHsEbn2xoP1xLesOTR9ZI5KDilCwgs1yA")
    );

    private final List<Ref<byte[]>> byteArrayMaskRefConfig1 = Arrays.asList(
            new Ref<>(Bytes.parseHex("1694bfba57009a05a9d9fa858c87bf35").array(), "amthd_b4azYqGWjqr91zfTkIhecGovdYgg"),
            new Ref<>(Bytes.parseHex("7fda28ebc01e4fafb4a87e9b1d7a05e6").array(), "8PHLunfjabvhUAh8T5AfxoS5zicUTr-Ptw"),
            new Ref<>(Bytes.parseHex("0ecf16594c6e85c2c368faf39d42d69f").array(), "jI2lIXW0iHuie_RDp2nqJ28Q57ApUH3Cfg"),
            new Ref<>(Bytes.parseHex("e1dba934e24987069a1442bb6c85cfe5").array(), "1dQzW1PVCrTFOsghZ74ycQbkoGXsI0XfnA"),
            new Ref<>(Bytes.parseHex("eda54c13baee8087c2477e34bfc3723f").array(), "XF0-blv46hsA9vbZwHuW1-WVc1gF_epT4g"),
            new Ref<>(Bytes.parseHex("f1fcffe66fc023d0e1bd8916d15929a0").array(), "LSzt3NxQYM2jqeqWxXvtis0577NAXvJZSw"),
            new Ref<>(Bytes.parseHex("a4321e5a577fbbe8b17a8e0808bc0af4").array(), "ExLorAYdmmKRL1j_juHxpmPbOyILpCwqLA"),
            new Ref<>(Bytes.parseHex("8499d3621689570fd06edb74a47103e8").array(), "pqdUVesH-er4cIOzF3RyGWcAyXoVs0Tocg")
    );

    @Test
    public void testLongReferenceTests() {
        checkReferenceTests(IdMaskRefConfigs.idMaskLongRefConfig1, longMaskRefConfig1);
        checkReferenceTests(IdMaskRefConfigs.idMaskLongRefConfig2, longMaskRefConfig2);
        checkReferenceTests(IdMaskRefConfigs.idMaskLongRefConfig3, longMaskRefConfig3);
        checkRandomReferenceTests(IdMaskRefConfigs.idMaskLongRefConfig4Random, longMaskRefConfig4Random);
    }

    @Test
    public void testUuidReferenceTests() {
        checkReferenceTests(IdMaskRefConfigs.idMaskUuidRefConfig1, uuidMaskRefConfig1);
        checkReferenceTests(IdMaskRefConfigs.idMaskUuidRefConfig2, uuidMaskRefConfig2);
        checkReferenceTests(IdMaskRefConfigs.idMaskUuidRefConfig3, uuidMaskRefConfig3);
        checkRandomReferenceTests(IdMaskRefConfigs.idMaskUuidRefConfig4Random, uuidMaskRefConfig4Random);
        checkReferenceTests(IdMaskRefConfigs.idMaskUuidRefConfig5HighSecurity, uuidMaskRefConfig5HighSecurity);
    }

    @Test
    public void testOtherTypesReferenceTests() {
        checkReferenceTests(IdMaskRefConfigs.idMaskBigIntegerRefConfig, bigIntegerMaskRefConfig1);
        checkByteArrayReferenceTests(IdMaskRefConfigs.idMaskByteArrayRefConfig, byteArrayMaskRefConfig1);
    }

    private <T> void checkReferenceTests(IdMask<T> idMask, List<Ref<T>> refList) {
        for (Ref<T> longRef : refList) {
            assertEquals(longRef.getEncoded(), idMask.mask(longRef.getId()));
            assertEquals(longRef.getId(), idMask.unmask(longRef.getEncoded()));
        }
    }

    private void checkByteArrayReferenceTests(IdMask<byte[]> idMask, List<Ref<byte[]>> refList) {
        for (Ref<byte[]> longRef : refList) {
            assertEquals(longRef.getEncoded(), idMask.mask(longRef.getId()));
            assertArrayEquals(longRef.getId(), idMask.unmask(longRef.getEncoded()));
        }
    }

    private <T> void checkRandomReferenceTests(IdMask<T> idMask, List<Ref<T>> refList) {
        for (Ref<T> longRef : refList) {
            assertNotEquals(longRef.getEncoded(), idMask.mask(longRef.getId()));
            assertEquals(longRef.getId(), idMask.unmask(longRef.getEncoded()));
        }
    }

}
