package org.thingsboard.example;

import org.thingsboard.client.tools.RestClient;
import org.thingsboard.server.common.data.Customer;
import org.thingsboard.server.common.data.EntityType;
import org.thingsboard.server.common.data.asset.Asset;
import org.thingsboard.server.common.data.group.EntityGroup;
import org.thingsboard.server.common.data.group.EntityGroupInfo;
import org.thingsboard.server.common.data.permission.GroupPermission;
import org.thingsboard.server.common.data.permission.Operation;
import org.thingsboard.server.common.data.role.Role;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RestClientExample {
    public static void main(String[] args) {
        // credentials for thingsboard
        String username = "tenant@thingsboard.org";
        String password = "tenant";

        // url for thingsboard
        String url = "http://localhost:8080";

        // creating new rest client and auth with credentials
        RestClient client = new RestClient(url);
        client.login(username, password);

        // creating customer
        Customer customer = new Customer();
        customer.setTitle("Customer_1");
        customer = client.saveCustomer(customer);

        // getting default customer group "Customer Users"
        List<EntityGroupInfo> customerGroups = client.getEntityGroupsByOwnerAndType(customer.getId(), EntityType.USER);
        EntityGroupInfo customerUsersGroup = customerGroups
                .stream()
                .filter(entityGroupInfo -> entityGroupInfo.getName().equals("Customer Users"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // creating asset
        Asset asset = new Asset();
        asset.setName("building_1");
        asset.setType("building");
        asset = client.saveAsset(asset);

        // creating entity group for assets
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.setName("buildings");
        entityGroup.setType(EntityType.ASSET);
        entityGroup = client.saveEntityGroup(entityGroup);

        // adding asset to entity group
        client.addEntitiesToEntityGroup(entityGroup.getId(), Collections.singletonList(asset.getId()));

        // creating role for user group
        Role role = client.createGroupRole("read_only", Arrays.asList(Operation.READ_ATTRIBUTES, Operation.READ_TELEMETRY));

        // creating permissions for user group with role by entity group with assets
        GroupPermission groupPermission = new GroupPermission();
        groupPermission.setEntityGroupId(entityGroup.getId());
        groupPermission.setRoleId(role.getId());
        groupPermission.setUserGroupId(customerUsersGroup.getId());
        groupPermission.setEntityGroupId(entityGroup.getId());
        groupPermission.setEntityGroupType(EntityType.ASSET);
        groupPermission = client.saveGroupPermission(groupPermission);
    }
}
