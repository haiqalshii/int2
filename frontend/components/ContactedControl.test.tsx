import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, vi } from "vitest";
import type { Company } from "@/types";
import { ContactedControl } from "./ContactedControl";

const mocks = vi.hoisted(() => ({ updateContacted: vi.fn() }));
vi.mock("@/lib/api", () => ({ updateContacted: mocks.updateContacted }));

const company: Company = {
  id: "62f921d6-bb4d-4d23-9c5e-2a98a4cb3021", name: "Aegis Cyber", description: null,
  website: null, email: null, phone: null, address: null, city: "Kuala Lumpur", state: "Kuala Lumpur",
  latitude: null, longitude: null, nacsaLicence: null, nacsaStatus: null, sourceUrl: null,
  services: [], contacted: false, contactedAt: null, lastVerifiedAt: null,
};

describe("ContactedControl", () => {
  beforeEach(() => mocks.updateContacted.mockReset());

  it("marks a company as contacted through the backend", async () => {
    const user = userEvent.setup();
    const updated = { ...company, contacted: true, contactedAt: "2026-08-15T08:00:00Z" };
    mocks.updateContacted.mockResolvedValue(updated);
    const onChange = vi.fn();
    render(<ContactedControl company={company} onChange={onChange} />);

    await user.click(screen.getByRole("button", { name: "Mark as contacted" }));
    expect(mocks.updateContacted).toHaveBeenCalledWith(company.id, true);
    await waitFor(() => expect(onChange).toHaveBeenCalledWith(updated));
  });

  it("can reverse the contacted state", async () => {
    const contacted = { ...company, contacted: true, contactedAt: "2026-08-15T08:00:00Z" };
    const updated = { ...company };
    mocks.updateContacted.mockResolvedValue(updated);
    const onChange = vi.fn();
    render(<ContactedControl company={contacted} onChange={onChange} />);
    fireEvent.click(screen.getByRole("button", { name: "Mark as not contacted" }));
    expect(mocks.updateContacted).toHaveBeenCalledWith(company.id, false);
    await waitFor(() => expect(onChange).toHaveBeenCalledWith(updated));
  });
});
