package com.vinabeef.demo;

import com.vinabeef.registry.domain.Barn;
import com.vinabeef.registry.domain.Cattle;
import com.vinabeef.registry.domain.CattleStatus;
import com.vinabeef.registry.domain.Gate;
import com.vinabeef.registry.persistence.BarnRepository;
import com.vinabeef.registry.persistence.CattleRepository;
import com.vinabeef.registry.persistence.GateRepository;
import com.vinabeef.shared.contract.GateType;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds one Dak Lak site: two barns, three gates and six cattle. It covers every demo case:
 * known cattle, an animal that belongs to the other barn, and a sold animal still wearing its tag.
 */
@Component
class DemoDataSeeder implements CommandLineRunner {

    private final BarnRepository barns;
    private final GateRepository gates;
    private final CattleRepository cattle;

    DemoDataSeeder(BarnRepository barns, GateRepository gates, CattleRepository cattle) {
        this.barns = barns;
        this.gates = gates;
        this.cattle = cattle;
    }

    @Override
    public void run(String... args) {
        if (barns.count() > 0) {
            return;
        }
        Barn a01 = barns.save(new Barn("DL-A01", "Dak Lak - Barn A01", 120));
        Barn a02 = barns.save(new Barn("DL-A02", "Dak Lak - Barn A02", 100));

        gates.saveAll(List.of(
                new Gate("DL-A01-G1", "North Gate", GateType.BIDIR, a01),
                new Gate("DL-A01-G2", "South Exit", GateType.EXIT, a01),
                new Gate("DL-A02-G1", "East Gate", GateType.BIDIR, a02)));

        Cattle sold = new Cattle("VN-0006", "704000000000006", "Red Sindhi", a02);
        sold.changeStatus(CattleStatus.SOLD);
        cattle.saveAll(List.of(
                new Cattle("VN-0001", "704000000000001", "Brahman", a01),
                new Cattle("VN-0002", "704000000000002", "Angus", a01),
                new Cattle("VN-0003", "704000000000003", "Brahman", a01),
                new Cattle("VN-0004", "704000000000004", "Droughtmaster", a02),
                new Cattle("VN-0005", "704000000000005", "Angus", a02),
                sold));
    }
}
