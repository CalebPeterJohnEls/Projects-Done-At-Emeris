namespace FunInAction.Mvc.Models;

/// <summary>
/// Wraps a Client with the extra flag the "Add / edit client" view needs
/// to switch its heading, button text, and whether the ID field is locked.
/// </summary>
public class ClientFormViewModel
{
    public Client Client { get; set; } = new();

    public bool IsEditing { get; set; }
}
