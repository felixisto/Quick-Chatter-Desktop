/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quickchatter.network.bluetooth.service;

public class BEServiceFilter {
    public int attributeID = 0; // The attribute id in the service attributes table
    public boolean requiresAuthorization = false;
    public boolean requiresEncryption = false;
    public boolean requiresMaster = false;
}
